package com.dbn.oracleAI.enums;

public enum ProviderType {

  COHERE("cohere"),
  OPENAI("openai"),
  OCI("oci");

  private final String action;

  ProviderType(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

}
