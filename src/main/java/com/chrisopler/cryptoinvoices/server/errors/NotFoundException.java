package com.chrisopler.cryptoinvoices.server.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** The invoice with the specified identifier could not be found. */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends Exception {
  public NotFoundException(String invoiceId) {
    super(String.format("An invoice with the id %s could not be found.", invoiceId));
  }
}
