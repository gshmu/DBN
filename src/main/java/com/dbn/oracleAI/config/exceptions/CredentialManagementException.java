package com.dbn.oracleAI.config.exceptions;

public class CredentialManagementException extends DatabaseOperationException {
  public CredentialManagementException(String message) {
    super(message);
  }

  public CredentialManagementException(String message, Throwable cause) {
    super(message, cause);
  }
}
