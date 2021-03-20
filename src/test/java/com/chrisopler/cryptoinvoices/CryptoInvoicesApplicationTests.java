package com.chrisopler.cryptoinvoices;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chrisopler.cryptoinvoices.server.blockchain.xrpl.CreateTestNetAddressResponse;
import com.chrisopler.cryptoinvoices.server.generated.model.Invoice;
import com.chrisopler.cryptoinvoices.server.generated.model.InvoiceCreateRequest;
import com.chrisopler.cryptoinvoices.server.generated.model.InvoiceStatus;
import com.google.common.primitives.UnsignedInteger;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CryptoInvoicesApplicationTests {

  @LocalServerPort int randomServerPort;
  private RestTemplate restTemplate = new RestTemplate();

  private XrplClient xrplClient =
      new XrplClient(HttpUrl.parse("https://s.altnet.rippletest.net:51234"));

  @Test
  public void positiveTest() throws Exception {

    InvoiceCreateRequest request = goodRequest();

    // -- create an invoice
    ResponseEntity<Invoice> createResult = createInvoice(request);
    assertEquals(200, createResult.getStatusCodeValue());
    assertEquals(InvoiceStatus.NEW, createResult.getBody().getInvoiceStatus());

    // -- expect th be able to fetch it.
    ResponseEntity<Invoice> getResult = getInvoice(createResult.getBody().getInvoiceId());
    assertEquals(200, createResult.getStatusCodeValue());
    assertEquals(createResult.getBody(), getResult.getBody());
  }

  /**
   * Validation tests.
   *
   * @throws Exception
   */
  @Test
  public void validationTests() throws Exception {
    // -- validate missing invoice amount
    {
      InvoiceCreateRequest request = goodRequest();
      request.setInvoiceAmount(null);
      badRequest(request);
    }
    // -- validate missing currency
    {
      InvoiceCreateRequest request = goodRequest();
      request.setCurrency(null);
      badRequest(request);
    }
    // -- validate due in seconds == 0
    {
      InvoiceCreateRequest request = goodRequest();
      request.setDueInSeconds(0);
      badRequest(request);
    }
    // -- validate null chain
    {
      InvoiceCreateRequest request = goodRequest();
      request.setChain(null);
      badRequest(request);
    }
    // -- validate negative amount
    {
      InvoiceCreateRequest request = goodRequest();
      request.setInvoiceAmount("-100");
      badRequest(request);
    }
    // -- validate non-numeric amount
    {
      InvoiceCreateRequest request = goodRequest();
      request.setInvoiceAmount("shouldbeanumber");
      badRequest(request);
    }
    // -- validate missing chain
    {
      InvoiceCreateRequest request = goodRequest();
      request.setChain("BTC");
      badRequest(request);
    }
  }

  @Test
  public void testTimeout() throws Exception, JsonRpcClientErrorException {

    // -- creating the paying wallet
    String testFaucet = "https://faucet.altnet.rippletest.net/accounts";
    ResponseEntity<CreateTestNetAddressResponse> payingAddress =
        restTemplate.postForEntity(testFaucet, "", CreateTestNetAddressResponse.class);
    assertEquals(200, payingAddress.getStatusCodeValue());
    WalletFactory walletFactory = DefaultWalletFactory.getInstance();
    Wallet payingWallet =
        walletFactory.fromSeed(payingAddress.getBody().getAccount().getSecret(), true);

    // -- create an invoice
    InvoiceCreateRequest request = goodRequest();
    request.setInvoiceAmount("100");
    request.setDueInSeconds(30);
    final ResponseEntity<Invoice> invoice = createInvoice(request);
    assertEquals(200, invoice.getStatusCodeValue());

    // -- send a partial payment
    sendPayment(
        payingWallet,
        payingAddress.getBody().getAccount().getClassicAddress(),
        invoice.getBody().getCryptoAddress(),
        new BigDecimal("10"));

    // -- wait until the invoice is marked partially paid
    await()
        .atMost(10, TimeUnit.SECONDS)
        .pollInterval(500, TimeUnit.MILLISECONDS)
        .until(
            () -> {
              ResponseEntity<Invoice> partiallyPaidInvoice =
                  getInvoice(invoice.getBody().getInvoiceId());
              return partiallyPaidInvoice
                      .getBody()
                      .getInvoiceStatus()
                      .equals(InvoiceStatus.PARTIALLY_PAID)
                  && new BigDecimal(partiallyPaidInvoice.getBody().getAmountPaid())
                          .compareTo(BigDecimal.TEN)
                      == 0;
            });
    // -- wait until the invoice has timed out
    await()
        .atMost(30, TimeUnit.SECONDS)
        .pollInterval(500, TimeUnit.MILLISECONDS)
        .until(
            () -> {
              ResponseEntity<Invoice> hopefullyExpired =
                  getInvoice(invoice.getBody().getInvoiceId());
              return hopefullyExpired.getBody().getInvoiceStatus().equals(InvoiceStatus.EXPIRED);
            });
  }

  @Test
  public void endToEndTest() throws Exception, JsonRpcClientErrorException {

    // -- create the paying wallet.
    String testFaucet = "https://faucet.altnet.rippletest.net/accounts";
    ResponseEntity<CreateTestNetAddressResponse> payingAddress =
        restTemplate.postForEntity(testFaucet, "", CreateTestNetAddressResponse.class);
    assertEquals(200, payingAddress.getStatusCodeValue());
    WalletFactory walletFactory = DefaultWalletFactory.getInstance();
    Wallet wallet = walletFactory.fromSeed(payingAddress.getBody().getAccount().getSecret(), true);

    // -- create an invoice request
    InvoiceCreateRequest request = goodRequest();
    request.setInvoiceAmount("100");
    final ResponseEntity<Invoice> invoice = createInvoice(request);
    assertEquals(200, invoice.getStatusCodeValue());

    // -- send a partial payment
    sendPayment(
        wallet,
        payingAddress.getBody().getAccount().getClassicAddress(),
        invoice.getBody().getCryptoAddress(),
        new BigDecimal("10"));

    // -- wait until the invoice is marked partially paid
    await()
        .atMost(10, TimeUnit.SECONDS)
        .pollInterval(500, TimeUnit.MILLISECONDS)
        .until(
            () -> {
              ResponseEntity<Invoice> partiallyPaidInvoice =
                  getInvoice(invoice.getBody().getInvoiceId());
              return partiallyPaidInvoice
                      .getBody()
                      .getInvoiceStatus()
                      .equals(InvoiceStatus.PARTIALLY_PAID)
                  && new BigDecimal(partiallyPaidInvoice.getBody().getAmountPaid())
                          .compareTo(BigDecimal.TEN)
                      == 0
                  && new BigDecimal(partiallyPaidInvoice.getBody().getAmountRemaining())
                          .compareTo(new BigDecimal("90"))
                      == 0;
            });

    // -- send the remaining amount
    sendPayment(
        wallet,
        payingAddress.getBody().getAccount().getClassicAddress(),
        invoice.getBody().getCryptoAddress(),
        new BigDecimal("90"));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .pollInterval(500, TimeUnit.MILLISECONDS)
        .until(
            () -> {
              ResponseEntity<Invoice> partiallyPaidInvoice =
                  getInvoice(invoice.getBody().getInvoiceId());
              return partiallyPaidInvoice.getBody().getInvoiceStatus().equals(InvoiceStatus.PAID)
                  && new BigDecimal(partiallyPaidInvoice.getBody().getAmountPaid())
                          .compareTo(new BigDecimal("100"))
                      == 0
                  && new BigDecimal(partiallyPaidInvoice.getBody().getAmountRemaining())
                          .compareTo(BigDecimal.ZERO)
                      == 0;
            });
  }

  /** NOTE: The send payment can be flaky causing tests to fail. Just re-run. */
  private void sendPayment(
      Wallet wallet, String sendingAddress, String destinationAddress, BigDecimal amount)
      throws JsonRpcClientErrorException {
    AccountInfoResult accountInfoResult =
        xrplClient.accountInfo(
            AccountInfoRequestParams.builder().account(Address.of(sendingAddress)).build());

    Payment payment =
        Payment.builder()
            .account(wallet.classicAddress())
            .amount(XrpCurrencyAmount.ofXrp(amount))
            .fee(XrpCurrencyAmount.ofDrops(1000))
            .sequence(accountInfoResult.accountData().sequence())
            .signingPublicKey(wallet.publicKey())
            .destination(Address.of(destinationAddress))
            .destinationTag(UnsignedInteger.ZERO)
            .build();

    SubmitResult<Payment> result = xrplClient.submit(wallet, payment);

    List<String> goodEngineResult = Arrays.asList("tesSUCCESS", "terQUEUED");

    assert (result.accepted()
        && result.status().isPresent()
        && result.status().get().equals("success")
        && result.engineResult().isPresent()
        && goodEngineResult.indexOf(result.engineResult().get()) > -1);

    await()
        .atMost(10, TimeUnit.SECONDS)
        .pollInterval(500, TimeUnit.MILLISECONDS)
        .until(
            () -> {
              try {
                TransactionResult<Payment> transaction =
                    xrplClient.transaction(
                        TransactionRequestParams.of(result.transactionResult().hash()),
                        Payment.class);
                return transaction.validated();
              } catch (JsonRpcClientErrorException e) {
                throw new RuntimeException(e);
              }
            });
  }

  private ResponseEntity<Invoice> createInvoice(InvoiceCreateRequest request) throws Exception {
    final String baseUrl = "http://localhost:" + randomServerPort + "/invoices";
    URI uri = new URI(baseUrl);
    return restTemplate.postForEntity(uri, request, Invoice.class);
  }

  private ResponseEntity<Invoice> getInvoice(String invoiceId) throws Exception {
    final String baseUrl = "http://localhost:" + randomServerPort + "/invoices/" + invoiceId;
    URI uri = new URI(baseUrl);
    return restTemplate.getForEntity(uri, Invoice.class);
  }

  private void badRequest(InvoiceCreateRequest request) {
    assertThrows(
        org.springframework.web.client.HttpClientErrorException.class,
        () -> {
          createInvoice(request);
        });
  }

  private InvoiceCreateRequest goodRequest() {
    InvoiceCreateRequest request = new InvoiceCreateRequest();
    request.setChain("XRPL");
    request.setCurrency("XRP");
    request.setChainEnvironment("TESTNET");
    request.setInvoiceAmount("100.000000");
    request.setDueInSeconds(60);
    return request;
  }
}
