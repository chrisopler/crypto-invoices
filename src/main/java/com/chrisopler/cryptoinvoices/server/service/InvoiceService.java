package com.chrisopler.cryptoinvoices.server.service;

import com.chrisopler.cryptoinvoices.server.errors.BadRequestException;
import com.chrisopler.cryptoinvoices.server.errors.NotFoundException;
import com.chrisopler.cryptoinvoices.server.errors.UnknownIntegrationException;
import com.chrisopler.cryptoinvoices.server.generated.model.Invoice;
import com.chrisopler.cryptoinvoices.server.generated.model.InvoiceCreateRequest;
import org.springframework.web.context.request.async.DeferredResult;

public interface InvoiceService {

  void getInvoice(String invoiceId, DeferredResult<Invoice> deferredResult)
      throws NotFoundException, UnknownIntegrationException, BadRequestException;

  void createInvoice(InvoiceCreateRequest request, DeferredResult<Invoice> deferredResult)
      throws BadRequestException, UnknownIntegrationException;
}
