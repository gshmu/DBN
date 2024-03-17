package com.dbn.oracleAI.config.exceptions;

public class DatabaseConnectionException extends DatabaseOperationException {
  public DatabaseConnectionException(String message) {
    super(message);
  }

  public DatabaseConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
