package com.chrisopler.cryptoinvoices.server.blockchain.xrpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestNetAddressResponse {
  private long balance;
  private long amount;
  private XRPLAccount account;
}
