package com.dbn.oracleAI.config.exceptions;

public class QueryExecutionException extends DatabaseOperationException {
  public QueryExecutionException(String message) {
    super(message);
  }

  public QueryExecutionException(String message, int codeError, Throwable cause){
    super(message, codeError, cause);
  }

  public QueryExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}

