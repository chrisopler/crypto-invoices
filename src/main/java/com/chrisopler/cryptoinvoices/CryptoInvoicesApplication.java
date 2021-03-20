package com.chrisopler.cryptoinvoices;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CryptoInvoicesApplication {

  public static void main(String[] args) {
    SpringApplication.run(CryptoInvoicesApplication.class, args);
  }

  @Bean
  public GroupedOpenApi actuatorApi() {
    return GroupedOpenApi.builder().group("Invoices").pathsToMatch("/invoices/**").build();
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .servers(
            Arrays.asList(
                new Server().url("http://localhost:8080/").description("The dev server"),
                new Server()
                    .url("http://invoices.iztcuintli.com:8080/")
                    .description("The prod server")))
        .info(
            new Info()
                .title("Crypto Invoices API")
                .version("0.0.1-SNAPSHOT")
                .description("This is the API for a simple invoice service")
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://github.com/chrisopler/crypto-invoices")));
  }
}
