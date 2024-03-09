package com.dbn.database.common.oracleAI;


import com.dbn.database.common.statement.CallableStatementOutput;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;


public class OracleProfilesInfo implements CallableStatementOutput {
  private String[] profiles;


  public String[] getProfiles() {
    return profiles;
  }

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    statement.registerOutParameter(1, Types.VARCHAR);
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    profiles = statement.getString(1).split(" ");
  }
}
