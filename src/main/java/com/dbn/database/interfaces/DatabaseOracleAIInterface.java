package com.dbn.database.interfaces;

import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.common.oracleAI.OracleCredentialsInfo;
import com.dbn.database.common.oracleAI.OracleProfilesInfo;
import com.dbn.database.common.oracleAI.OracleQueryOutput;

import java.sql.SQLException;

public interface DatabaseOracleAIInterface extends DatabaseInterface{

  void setupCredential(DBNConnection connection, String credentialName, String apiName, String apiKey) throws SQLException;

  void setupProfile(DBNConnection connection, String profileName, String provider, String credentialName) throws SQLException;

  OracleCredentialsInfo listCredentials(DBNConnection connection) throws SQLException;

  OracleProfilesInfo listProfiles(DBNConnection connection) throws SQLException;

  void pickProfile(DBNConnection connection, String profileName) throws SQLException;

  OracleQueryOutput queryProfile(DBNConnection connection, String type, String text) throws SQLException;


}
