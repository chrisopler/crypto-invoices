package com.chrisopler.cryptoinvoices.server.blockchain;

import com.chrisopler.cryptoinvoices.server.errors.UnknownIntegrationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class BlockchainIntegrationFactory {
  private Map<String, BlockchainIntegration> REGISTRY = new ConcurrentHashMap<>();

  public BlockchainIntegrationFactory(List<BlockchainIntegration> integrationList) {
    integrationList.stream()
        .forEach(
            (i) -> {
              REGISTRY.put(i.chainKey(), i);
            });
  }

  public BlockchainIntegration getIntegration(String chain, String chainEnvironment)
      throws UnknownIntegrationException {
    return Optional.ofNullable(REGISTRY.get(key(chain, chainEnvironment)))
        .orElseThrow(
            () ->
                new UnknownIntegrationException(
                    String.format(
                        "We do not have an integration for the %s chain and %s environment",
                        chain, chainEnvironment)));
  }

  private String key(String chain, String chainEnvironment) {
    return String.format("%s/%s", chain, chainEnvironment);
  }
}
