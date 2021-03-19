package com.chrisopler.cryptoinvoices.server.service;

import com.chrisopler.cryptoinvoices.server.blockchain.BlockchainIntegration;
import com.chrisopler.cryptoinvoices.server.blockchain.BlockchainIntegrationFactory;
import com.chrisopler.cryptoinvoices.server.errors.BadRequestException;
import com.chrisopler.cryptoinvoices.server.errors.NotFoundException;
import com.chrisopler.cryptoinvoices.server.errors.UnknownIntegrationException;
import com.chrisopler.cryptoinvoices.server.generated.model.Invoice;
import com.chrisopler.cryptoinvoices.server.generated.model.InvoiceCreateRequest;
import com.chrisopler.cryptoinvoices.server.generated.model.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

@Component
public class InvoiceServiceImpl implements InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final BlockchainIntegrationFactory blockchainIntegrationFactory;

  public InvoiceServiceImpl(
      InvoiceRepository invoiceRepository,
      BlockchainIntegrationFactory blockchainIntegrationFactory) {
    this.invoiceRepository = invoiceRepository;
    this.blockchainIntegrationFactory = blockchainIntegrationFactory;
  }

  @Override
  public void getInvoice(String invoiceId, DeferredResult<Invoice> deferredResult)
      throws NotFoundException, UnknownIntegrationException, BadRequestException {

    Invoice invoice =
        this.invoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new NotFoundException(invoiceId));

    if (isComplete(invoice.getInvoiceStatus())) {
      deferredResult.setResult(invoice);
      return;
    }

    if (isExpired(invoice)) {
      /*
       * Normally gets are not mutating. Future work includes adding a background scheduler to
       * to perform expiration and paid status checks.  As such this method would be a pure get.
       */
      invoice.setInvoiceStatus(InvoiceStatus.EXPIRED);
      this.invoiceRepository.save(invoice);
      deferredResult.setResult(invoice);
      return;
    }

    BlockchainIntegration blockchainIntegration =
        this.blockchainIntegrationFactory.getIntegration(
            invoice.getChain(),
            Optional.ofNullable(invoice.getChainEnvironment()).orElse("default"));
    BigDecimal b = blockchainIntegration.getBalance(invoice.getCryptoAddress());

    invoice.setAmountPaid(toString(b, blockchainIntegration));
    if (isPaid(invoice)) {
      invoice.setInvoiceStatus(InvoiceStatus.PAID);
      this.invoiceRepository.save(invoice);
      deferredResult.setResult(invoice);
    } else if (isPartiallyPaid(invoice)) {
      invoice.setInvoiceStatus(InvoiceStatus.PARTIALLY_PAID);
      this.invoiceRepository.save(invoice);
      deferredResult.setResult(invoice);
    } else {
      // this shouldn't happen
      deferredResult.setResult(invoice);
    }
  }

  private String toString(BigDecimal amount, BlockchainIntegration blockchainIntegration) {
    return amount
        .setScale(
            blockchainIntegration.getDecimalPrecision(), blockchainIntegration.getRoundingMode())
        .toPlainString();
  }

  private boolean isComplete(InvoiceStatus status) {
    return status.equals(InvoiceStatus.PAID) || status.equals(InvoiceStatus.EXPIRED);
  }

  private boolean isExpired(Invoice invoice) {
    return invoice.getDueDate().isBefore(Instant.now());
  }

  private boolean isPaid(Invoice invoice) {
    return new BigDecimal(invoice.getAmountPaid())
            .compareTo(new BigDecimal(invoice.getInvoiceAmount()))
        >= 0;
  }

  private boolean isPartiallyPaid(Invoice invoice) {
    return new BigDecimal(invoice.getAmountPaid())
                .compareTo(new BigDecimal(invoice.getInvoiceAmount()))
            < 0
        && new BigDecimal(invoice.getAmountPaid()).compareTo(BigDecimal.ZERO) > 0;
  }

  @Override
  public void createInvoice(InvoiceCreateRequest request, DeferredResult<Invoice> deferredResult)
      throws BadRequestException, UnknownIntegrationException {

    // let us validate
    if (request.getChain() == null) {
      throw new BadRequestException("Missing chain");
    }
    if (request.getInvoiceAmount() == null) {
      throw new BadRequestException("Missing invoice_amount");
    }
    if (request.getCurrency() == null) {
      throw new BadRequestException("Missing currency");
    }
    if (request.getDueInSeconds() <= 0) {
      throw new BadRequestException("Missing or invalid due_in_seconds");
    }
    try {
      if (new BigDecimal(request.getInvoiceAmount()).compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("invoice_amount must be greater than zero");
      }
    } catch (NumberFormatException e) {
      throw new BadRequestException("invoice_amount must be a numeric value");
    }

    BlockchainIntegration blockchainIntegration =
        this.blockchainIntegrationFactory.getIntegration(
            request.getChain(),
            Optional.ofNullable(request.getChainEnvironment()).orElse("default"));

    Mono<String> cryptoAddress = blockchainIntegration.createNewCryptoAddress();
    cryptoAddress
        // -- I couldn't figure out how to trigger an exception properly
        .doOnError((e) -> new BadRequestException(e.getMessage()))
        .subscribe(
            (a) -> {
              Invoice invoice =
                  this.invoiceRepository.save(
                      Invoice.builder()
                          .invoiceAmount(request.getInvoiceAmount())
                          .invoiceStatus(InvoiceStatus.NEW)
                          .amountPaid(
                              BigDecimal.ZERO
                                  .setScale(
                                      blockchainIntegration.getDecimalPrecision(),
                                      blockchainIntegration.getRoundingMode())
                                  .toPlainString())
                          .chain(request.getChain())
                          .chainEnvironment(request.getChainEnvironment())
                          .cryptoAddress(a)
                          .dueDate(
                              Instant.now().plus(request.getDueInSeconds(), ChronoUnit.SECONDS))
                          .currency(request.getCurrency())
                          .build());
              deferredResult.setResult(invoice);
            });
  }
}
