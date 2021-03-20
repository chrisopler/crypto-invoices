package com.chrisopler.cryptoinvoices.server.blockchain;

import com.chrisopler.cryptoinvoices.server.errors.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import reactor.core.publisher.Mono;

/** Represents the contract for an integration with a blockchain. */
public interface BlockchainIntegration {

  String chainKey();

  /**
   * Create a new address on the chain and environment. Note that normally in the address creation
   * process, the address is associated with a private key to allow for moving the funds out of the
   * address. In this case, the funds sent to the address are locked in that address permanently as
   * we are discarding the key;
   *
   * @return
   */
  Mono<String> createNewCryptoAddress() throws BadRequestException;

  BigDecimal getBalance(String cryptoAddress) throws BadRequestException;

  int getDecimalPrecision();

  RoundingMode getRoundingMode();
}
