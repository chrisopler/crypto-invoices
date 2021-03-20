package com.chrisopler.cryptoinvoices.server.rest;

import com.chrisopler.cryptoinvoices.server.errors.BadRequestException;
import com.chrisopler.cryptoinvoices.server.errors.NotFoundException;
import com.chrisopler.cryptoinvoices.server.errors.UnknownIntegrationException;
import com.chrisopler.cryptoinvoices.server.generated.model.ErrorResponse;
import com.chrisopler.cryptoinvoices.server.generated.model.Invoice;
import com.chrisopler.cryptoinvoices.server.generated.model.InvoiceCreateRequest;
import com.chrisopler.cryptoinvoices.server.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class InvoiceController {

  private InvoiceService invoiceService;

  public InvoiceController(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @Operation(
      method = "GET",
      parameters = {
        @Parameter(in = ParameterIn.PATH, name = "invoiceId", description = "Invoice Id")
      },
      operationId = "getInvoiceById",
      summary = "Get an Invoice by invoiceId",
      responses = {
        @ApiResponse(
            description = "Successful Operation",
            responseCode = "200",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
      })
  @GetMapping("/invoices/{invoiceId}")
  public DeferredResult<Invoice> getInvoice(final @PathVariable String invoiceId)
      throws NotFoundException, UnknownIntegrationException, BadRequestException {
    DeferredResult<Invoice> result = new DeferredResult<>();
    invoiceService.getInvoice(invoiceId, result);
    return result;
  }

  @Operation(
      method = "POST",
      operationId = "createInvoice",
      summary = "Create a new Invoice.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the Item to be created",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = InvoiceCreateRequest.class),
                      mediaType = MediaType.APPLICATION_JSON_VALUE)),
      responses = {
        @ApiResponse(
            description = "Successful Operation",
            responseCode = "200",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
      })
  @PostMapping(path = "/invoices", consumes = MediaType.APPLICATION_JSON_VALUE)
  public DeferredResult<Invoice> createInvoice(
      @org.springframework.web.bind.annotation.RequestBody
          final InvoiceCreateRequest invoiceCreateRequest)
      throws BadRequestException, UnknownIntegrationException {
    DeferredResult<Invoice> result = new DeferredResult<>();
    invoiceService.createInvoice(invoiceCreateRequest, result);
    return result;
  }
}
