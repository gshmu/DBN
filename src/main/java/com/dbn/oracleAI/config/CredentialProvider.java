package com.dbn.oracleAI.config;

import lombok.Getter;

@Getter
public class CredentialProvider implements AttributeInput {
  protected String credentialName;

  public CredentialProvider(String credentialName) {
    this.credentialName = credentialName;
  }

  @Override
  public boolean isValid() {
    return credentialName != null && !credentialName.contains("'");
  }

  @Override
  public String format(){
    return "";
  };
}
