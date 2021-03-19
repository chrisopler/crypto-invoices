package com.chrisopler.cryptoinvoices.server.blockchain.xrpl;

import com.chrisopler.cryptoinvoices.server.blockchain.BlockchainIntegration;
import com.chrisopler.cryptoinvoices.server.errors.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import reactor.core.publisher.Mono;

@Component
public class XRPLTestNetBlockchainIntegration implements BlockchainIntegration {

  private static final String CREATE_FAUCET_ENDPOINT =
      "https://faucet.altnet.rippletest.net/accounts";

  private static final String TEST_NET_RIPPLED_ENDPOINT = "https://s.altnet.rippletest.net:51234";
  private static final String CHAIN = "XRPL";
  private static final String ENVIRONMENT = "TESTNET";
  private static final Long INITIAL_FAUCET_AMOUNT = 1_000L;
  private static final Long DROPS_PER_XRP = 1_000_000L;

  private XrplClient xrplClient = new XrplClient(HttpUrl.parse(TEST_NET_RIPPLED_ENDPOINT));

  @Override
  public String chainKey() {
    return String.format("%s/%s", CHAIN, ENVIRONMENT);
  }

  public BigDecimal getBalance(String cryptoAddress) throws BadRequestException {
    try {
      AccountInfoResult result =
          xrplClient.accountInfo(
              AccountInfoRequestParams.builder().account(Address.of(cryptoAddress)).build());

      return BigDecimal.valueOf(
          (result.accountData().balance().value().longValue() / DROPS_PER_XRP)
              - INITIAL_FAUCET_AMOUNT);
    } catch (JsonRpcClientErrorException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @Override
  public int getDecimalPrecision() {
    return 6;
  }

  @Override
  public RoundingMode getRoundingMode() {
    return RoundingMode.HALF_DOWN;
  }

  @Override
  public Mono<String> createNewCryptoAddress() {
    return WebClient.create()
        .post()
        .uri(CREATE_FAUCET_ENDPOINT)
        .retrieve()
        .bodyToMono(CreateTestNetAddressResponse.class)
        .map((a) -> a.getAccount().getAddress());
  }
}
