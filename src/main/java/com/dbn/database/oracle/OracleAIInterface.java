package com.dbn.database.oracle;

import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.common.DatabaseInterfaceBase;
import com.dbn.database.common.oracleAI.OracleCredentialsInfo;
import com.dbn.database.common.oracleAI.OracleProfilesInfo;
import com.dbn.database.common.oracleAI.OracleQueryOutput;
import com.dbn.database.interfaces.DatabaseInterfaces;
import com.dbn.database.interfaces.DatabaseOracleAIInterface;

import java.sql.SQLException;

public class OracleAIInterface extends DatabaseInterfaceBase implements DatabaseOracleAIInterface {
  public OracleAIInterface(DatabaseInterfaces provider) {
    super("oracle_ai_interface.xml", provider);
  }


  @Override
  public void setupCredential(DBNConnection connection, String credentialName, String apiName, String apiKey) throws SQLException {
    executeCall(connection, null, "setup-credential", credentialName, apiName, apiKey);

  }

  @Override
  public void setupProfile(DBNConnection connection, String profileName, String provider, String credentialName) throws SQLException {
    executeCall(connection, null, "setup-profile", profileName, profileName, credentialName);
  }

  @Override
  public OracleCredentialsInfo listCredentials(DBNConnection connection) throws SQLException {
    return executeCall(connection, new OracleCredentialsInfo(), "list-credentials");
  }

  @Override
  public OracleProfilesInfo listProfiles(DBNConnection connection) throws SQLException {
    return executeCall(connection, new OracleProfilesInfo(), "list-profiles");
  }


  @Override
  public void pickProfile(DBNConnection connection, String profileName) throws SQLException {
    executeCall(connection, null, "pick-profile", profileName);
  }

  @Override
  public OracleQueryOutput queryProfile(DBNConnection connection, String type, String text) throws SQLException {
    return executeCall(connection, new OracleQueryOutput(), "ai-query", type, text);
  }

}
