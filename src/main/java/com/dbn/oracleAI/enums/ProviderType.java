package com.dbn.oracleAI.enums;

import lombok.Getter;

@Getter
public enum ProviderType {

  COHERE("cohere"),
  OPENAI("openai"),
  OCI("oci");

  private final String action;

  ProviderType(String action) {
    this.action = action;
  }

}
