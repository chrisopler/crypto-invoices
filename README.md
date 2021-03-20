# Crypto Invoices

### Introduction

This is a simple example of a service providing basic functionality to create and manage invoices to
be paid with digital currencies.

* A User is able to use the service create an invoice for a specific amount and in a specific
  currency on a specific chain.
* A new digital currency address is created for each invoice
* When funds are sent to the currency, the invoice is automatically adjusted to reflect the payment.
* Invoices have 4 states:
    * NEW
    * EXPIRED
    * PARTIALLY_PAID
    * PAID

## Operating

Because this service is built with Spring Boot and Maven, the build generates an executable jar
file.

To run from the command line:

* git clone https://github.com/chrisopler/crypto-invoices.git
* cd crypto-invoices
* mvn clean install
* java -jar ./target/crypto-invoices-0.0.1-SNAPSHOT.jar

## Swagger

This service provides a RESTful interface documented with OpenApi. To access the developers console:

* run the service as described above
* navigate to http://localhost:8080/swagger-ui.html with a browser.