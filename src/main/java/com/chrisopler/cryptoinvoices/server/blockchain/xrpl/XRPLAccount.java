package com.chrisopler.cryptoinvoices.server.blockchain.xrpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class XRPLAccount {
  private String xAddress;
  private String secret;
  private String classicAddress;
  private String address;
}
