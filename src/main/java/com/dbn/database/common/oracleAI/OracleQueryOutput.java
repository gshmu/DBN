package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OracleQueryOutput implements CallableStatementOutput {
  private String queryOutput;


  public String getQueryOutput() {
    return queryOutput;
  }

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    System.out.println(statement.getParameterMetaData().getParameterCount());
    statement.registerOutParameter(1, Types.VARCHAR);
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    queryOutput = statement.getString(1);
  }
}
