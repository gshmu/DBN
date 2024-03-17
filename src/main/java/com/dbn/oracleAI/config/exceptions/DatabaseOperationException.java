package com.dbn.oracleAI.config.exceptions;

import lombok.Getter;

@Getter
public class DatabaseOperationException extends Exception {

  private int errorCode;

  public DatabaseOperationException(String message, int errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }


  public DatabaseOperationException(String message) {
    super(message);
  }

  public DatabaseOperationException(String message, Throwable cause) {
  }
}
