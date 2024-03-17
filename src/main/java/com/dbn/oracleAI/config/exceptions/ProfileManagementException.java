package com.dbn.oracleAI.config.exceptions;

public class ProfileManagementException extends DatabaseOperationException {
  public ProfileManagementException(String message) {
    super(message);
  }

  public ProfileManagementException(String message, Throwable cause) {
    super(message, cause);
  }
}

