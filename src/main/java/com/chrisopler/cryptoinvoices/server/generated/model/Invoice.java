package com.chrisopler.cryptoinvoices.server.generated.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

/**
 * Represents a request for payment for services rendered. Payment in the currency specified is to
 * be made to the crypto address specified.
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "invoices")
public class Invoice {

  @org.springframework.data.annotation.Id
  @javax.persistence.Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid")
  @Schema(
      name = "invoice_id",
      description = "The globally unique identifier of the invoice",
      example = "589",
      required = true)
  @JsonProperty("invoice_id")
  @Column(name = "invoice_id", nullable = false)
  private String invoiceId;

  @JsonProperty("invoice_amount")
  @Column(name = "invoice_amount", nullable = false)
  @Schema(
      name = "invoice_amount",
      description = "The amount in the currency due.",
      example = "100.000000",
      required = true)
  private String invoiceAmount;

  @JsonProperty("amount_paid")
  @Column(name = "amount_paid", nullable = false)
  @Schema(
      name = "amount_paid",
      description = "The amount paid to date",
      example = "0.000000",
      required = true)
  private String amountPaid;

  @JsonProperty("amount_remaining")
  @Column(name = "amount_remaining", nullable = false)
  @Schema(
      name = "amount_remaining",
      description = "The amount remaining to be paid",
      example = "100.000000",
      required = true)
  private String amountRemaining;

  @JsonProperty("crypto_address")
  @Column(name = "crypto_address", nullable = false)
  @Schema(
      name = "crypto_address",
      description = "The crypto address to which the amount due should be paid",
      example = "r9JuxGPccGjMGr54t4JXUDsemAcECQWXTf",
      required = true)
  private String cryptoAddress;

  @JsonProperty("currency")
  @Column(name = "currency", nullable = false)
  @Schema(
      name = "currency",
      description = "The currency of the invoice",
      example = "XRP",
      required = true)
  private String currency;

  @JsonProperty("chain")
  @Column(name = "chain", nullable = false)
  @Schema(
      name = "chain",
      description = "The identifier of the blockchain.",
      example = "XRPL",
      required = true)
  private String chain;

  @JsonProperty("chain_environment")
  @Column(name = "chain_environment", nullable = true)
  @Schema(
      name = "chain_environment",
      description = "Some block chains support multiple environments.  Specify that here.",
      example = "TESTNET",
      required = false)
  private String chainEnvironment;

  @JsonProperty("invoice_status")
  @Column(name = "invoice_status", nullable = false)
  @Schema(
      name = "invoice_status",
      description = "The status of the invoice",
      example = "NEW",
      required = true)
  private InvoiceStatus invoiceStatus;

  @JsonProperty("due_date")
  @Column(name = "due_date", nullable = false)
  @Schema(
      name = "due_date",
      description = "The due date of the invoice",
      example = "2021-03-17T12:42:59.663Z",
      required = true)
  private Instant dueDate;
}
