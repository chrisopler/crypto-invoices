package com.chrisopler.cryptoinvoices.server.generated.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** A request to create an invoice */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class InvoiceCreateRequest {

  @JsonProperty("invoice_amount")
  @Schema(
      name = "invoice_amount",
      description = "The amount in the currency due.",
      example = "34.000001",
      required = true)
  private String invoiceAmount;

  @JsonProperty("currency")
  @Schema(
      name = "currency",
      description = "The currency of the invoice",
      example = "XRP",
      required = true)
  private String currency;

  @JsonProperty("chain")
  @Schema(
      name = "chain",
      description = "The identifier of the blockchain.",
      example = "XRPL",
      required = false)
  private String chain;

  @JsonProperty("chain_environment")
  @Schema(
      name = "chain_environment",
      description = "Some block chains support multiple environments.  Specify that here.",
      example = "TESTNET",
      required = false)
  private String chainEnvironment;

  @JsonProperty("due_in_seconds")
  @Schema(
      name = "due_in_seconds",
      description = "The number of seconds from the time of creation that the invoice is due.",
      example = "60",
      defaultValue = "60",
      required = true)
  private int dueInSeconds;
}
