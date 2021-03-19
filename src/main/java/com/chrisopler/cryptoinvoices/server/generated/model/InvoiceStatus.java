package com.chrisopler.cryptoinvoices.server.generated.model;

/**
 * The payment status of the invoice. Note that we have added `NEW` as an additional status as the
 * other three do not cover the potential states within the scope of the exercise.
 */
public enum InvoiceStatus {
  NEW,
  EXPIRED,
  PARTIALLY_PAID,
  PAID
}
