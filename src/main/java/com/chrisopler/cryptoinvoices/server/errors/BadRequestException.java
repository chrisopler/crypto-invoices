package com.chrisopler.cryptoinvoices.server.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The request to create an invoice contained invalid parameters. The invoice could not be created.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends Exception {
  public BadRequestException(String msg) {
    super(msg);
  }
}
