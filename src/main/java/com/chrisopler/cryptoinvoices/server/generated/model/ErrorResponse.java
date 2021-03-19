package com.chrisopler.cryptoinvoices.server.generated.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Representation of a message returned in the case of an error on the server in response to one of
 * your requests to the API.
 */
@Data
public class ErrorResponse {

  @Schema(
      description = "The time the error occurred in milliseconds since the epoch.",
      example = "1615816008722",
      required = true)
  private long timestamp;

  @Schema(description = "The path of the http call", example = "/invoice/8768", required = true)
  private String path;

  @Schema(description = "The http status code", example = "404", required = true)
  private int status;

  @Schema(
      description = "The text version of the http status code",
      example = "Not Found",
      required = true)
  private String error;

  @Schema(
      description = "A message transmitted by the server related to the error",
      example = "An invoice with the id 8768 could not be found.",
      required = true)
  private String message;

  @Schema(
      description =
          "A string identifying the specific request.  Use this to communicate with the "
              + "administrator should you have questions about the error.",
      example = "1aa8d9fc-12",
      required = true)
  private String requestId;

  @Schema(
      description =
          "Specific information about where the error occurred.  Note: In a real system, this information would be filtered out.",
      example =
          "com.chrisopler.cryptoinvoices.server.errors.NotFoundException: {\\\"id\\\":\\\"ho\\\"}\\n\\tat com.chrisopler.cryptoinvoices.server.rest.InvoiceController.getInvoice(InvoiceController.java:46)\\n\\tSuppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: \\nError has been observed at the following site(s):\\n\\t|_ checkpoint ⇢ HTTP GET \\\"/invoice/8768\\\" [ExceptionHandlingWebHandler]\\nStack trace:\\n\\",
      required = true)
  private String trace;
}
