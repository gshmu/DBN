package com.dbn.database.interfaces;

import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.common.oracleAI.*;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.enums.ActionAIType;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for managing Oracle AI profiles and credentials.
 * Provides functionalities for creating, updating, and deleting credentials and profiles,
 * executing AI-related queries, and listing database tables, views, credentials, and profiles.
 */
public interface DatabaseOracleAIInterface extends DatabaseInterface {

  /**
   * Creates a new credential for accessing external services or databases.
   *
   * @param connection The database connection.
   * @param credentialName The name of the credential to create.
   * @param args The args for creating the credential.
   * @throws SQLException If a database access error occurs.
   */
  void createCredential(DBNConnection connection, CredentialProvider credentialAttributes) throws CredentialManagementException;

  /**
   * Removes an existing credential from the database.
   *
   * @param connection The database connection.
   * @param credentialName The name of the credential to remove.
   * @throws SQLException If a database access error occurs.
   */
  void dropCredential(DBNConnection connection, String credentialName) throws CredentialManagementException;

  /**
   * Updates an attribute of an existing credential.
   *
   * @param connection The database connection.
   * @param credentialName The name of the credential to update.
   * @param attributeName The name of the attribute to update.
   * @param attributeValue The new value for the attribute.
   * @throws SQLException If a database access error occurs.
   */
  void setCredentialAttribute(DBNConnection connection, String credentialName, String attributeName, String attributeValue) throws CredentialManagementException;

  /**
   * Creates a new AI profile for executing AI-related operations.
   *
   * @param connection The database connection.
   * @param profileName The name of the new AI profile.
   * @param provider The provider of the AI service.
   * @param credentialName The name of the credential associated with this profile.
   * @throws SQLException If a database access error occurs.
   */
  void createProfile(DBNConnection connection, Profile profileAttributes) throws ProfileManagementException;

  /**
   * Updates an attribute of an existing AI profile.
   *
   * @param connection The database connection.
   * @param profileName The name of the AI profile to update.
   * @param attributeName The name of the attribute to update.
   * @param attributeValue The new value for the attribute.
   * @throws SQLException If a database access error occurs.
   */
  void setProfileAttribute(DBNConnection connection, String profileName, String attributeName, String attributeValue) throws ProfileManagementException;

  /**
   * Deletes an existing AI profile from the database.
   *
   * @param connection The database connection.
   * @param profileName The name of the AI profile to delete.
   * @throws SQLException If a database access error occurs.
   */
  void dropProfile(DBNConnection connection, String profileName) throws ProfileManagementException;

  /**
   * Executes an AI-related query using a specified action and text.
   *
   * @param connection The database connection.
   * @param action The AI action to perform (e.g., "translate", "analyze").
   * @param text The text or query to process.
   * @return The result of the AI query execution.
   * @throws SQLException If a database access error occurs.
   */
  OracleQueryOutput executeQuery(DBNConnection connection, ActionAIType action, String profile, String text) throws QueryExecutionException;

  /**
   * Lists all tables available in the current database schema.
   *
   * @param connection The database connection.
   * @return A list of tables in the database schema.
   * @throws SQLException If a database access error occurs.
   */
  OracleTablesList listTables(DBNConnection connection) throws DatabaseOperationException;

  /**
   * Lists all views available in the current database schema.
   *
   * @param connection The database connection.
   * @return A list of views in the database schema.
   * @throws SQLException If a database access error occurs.
   */
  OracleViewsList listViews(DBNConnection connection) throws DatabaseOperationException;

  /**
   * Lists all credentials stored in the database.
   *
   * @param connection The database connection.
   * @return Information about stored credentials.
   * @throws SQLException If a database access error occurs.
   */
  List<CredentialProvider> listCredentials(DBNConnection connection) throws DatabaseOperationException;

  /**
   * Lists all AI profiles stored in the database.
   *
   * @param connection The database connection.
   * @return Information about stored AI profiles.
   * @throws SQLException If a database access error occurs.
   */
  List<Profile> listProfiles(DBNConnection connection) throws DatabaseOperationException;

}
