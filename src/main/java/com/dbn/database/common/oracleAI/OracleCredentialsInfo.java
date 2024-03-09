package com.dbn.database.common.oracleAI;


import com.dbn.database.common.statement.CallableStatementOutput;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;


public class OracleCredentialsInfo implements CallableStatementOutput {
  private String[] credentials;


  public String[] getCredentials() {
    return credentials;
  }

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    System.out.println(statement.getParameterMetaData().getParameterCount());
    statement.registerOutParameter(1, Types.VARCHAR);
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    credentials = statement.getString(1).split(" ");
  }
}
