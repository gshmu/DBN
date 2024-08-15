package com.dbn.database.oracle;

import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.common.DatabaseInterfaceBase;
import com.dbn.database.common.oracleAI.*;
import com.dbn.database.interfaces.DatabaseInterfaces;
import com.dbn.database.interfaces.DatabaseOracleAIInterface;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.types.DatabaseObjectType;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OracleAIInterface extends DatabaseInterfaceBase implements DatabaseOracleAIInterface {

  public OracleAIInterface(DatabaseInterfaces provider) {
    super("oracle_ai_interface.xml", provider);
  }

  @Override
  public void createCredential(DBNConnection connection, Credential credentialAttributes) throws CredentialManagementException {
    try {
      executeCall(connection, null, "create-credential", credentialAttributes.getCredentialName(), credentialAttributes.toAttributeMap());
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to create credential: " + credentialAttributes.getCredentialName(), e);
    }
  }

  @Override
  public void dropCredential(DBNConnection connection, String credentialName) throws CredentialManagementException {
    try {
      executeCall(connection, null, "drop-credential", credentialName);
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to drop credential: " + credentialName, e);
    }
  }

  @Override
  public void setCredentialAttribute(DBNConnection connection, Credential credential) throws CredentialManagementException {

    List<String> updatedList = credential.toUpdatingAttributeList();

    //TODO make these requests run simultaneously
    for (String updatedAttr : updatedList) {
      try {
        executeCall(connection, null, "update-credential", updatedAttr);
      } catch (SQLException e) {
        throw new CredentialManagementException("Failed to set credential attribute: " + credential.getCredentialName(), e);
      }
    }

  }

  @Override
  public void createProfile(DBNConnection connection, Profile profileAttributes) throws ProfileManagementException {
    try {
      executeCall(connection, null, "create-profile", profileAttributes.getProfileName(), profileAttributes.toAttributeMap());
    } catch (SQLException e) {
      throw new ProfileManagementException("Failed to create profile: " + profileAttributes.getProfileName() + "\n" + e.getMessage(), e);
    }
  }

  @Override
  public void dropProfile(DBNConnection connection, String profileName) throws ProfileManagementException {
    try {
      executeCall(connection, null, "drop-profile", profileName);
    } catch (SQLException e) {
      throw new ProfileManagementException("Failed to drop profile: " + profileName, e);
    }
  }

  @Override
  public void setProfileAttributes(DBNConnection connection, Profile profile) throws ProfileManagementException {
    if (log.isDebugEnabled()) {
      log.debug("setProfileAttributes for " + profile);
      log.debug("   attribute map " + profile.toAttributeMap());
    }
    try {
      executeCall(connection, null, "update-profile", profile.toAttributeMap());
    } catch (SQLException e) {
      throw new ProfileManagementException("Failed to set profile attribute: " + profile.getProfileName(), e);
    }
  }

  @Override
  public OracleQueryOutput executeQuery(DBNConnection connection, ActionAIType action, String profile, String text, String model) throws QueryExecutionException {
    try {
      return executeCall(connection, new OracleQueryOutput(), "ai-query", profile, action, text.replace("'", "''"), "{\"model\":\"" + model + "\"}");
    } catch (SQLException e) {
      throw new QueryExecutionException("Failed to query\n", e);
    }
  }


  @Override
  public OracleTablesList listTables(DBNConnection connection) throws DatabaseOperationException {
    try {
      return executeCall(connection, new OracleTablesList(), "list-tables");
    } catch (SQLException e) {
      throw new DatabaseOperationException("Failed to list tables", e);
    }
  }

  @Override
  public OracleViewsList listViews(DBNConnection connection) throws DatabaseOperationException {
    try {
      return executeCall(connection, new OracleViewsList(), "list-views");
    } catch (SQLException e) {
      throw new DatabaseOperationException("Failed to list views", e);
    }
  }


  @Override
  public List<Credential> listCredentials(DBNConnection connection) throws CredentialManagementException {
    try {
      List<Credential> credentials = executeCall(connection, new OracleCredentialsDetailedInfo(), "list-credentials-detailed").getCredentialsProviders();
      return credentials;
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to list credentials", e);
    }
  }

  @Override
  public List<Profile> listProfiles(DBNConnection connection) throws ProfileManagementException {
    try {
      List<Profile> profileList = executeCall(connection, new OracleProfilesAttributesInfo(), "list-profiles-detailed").getProfileList();
      return profileList;
    } catch (SQLException e) {
      throw new ProfileManagementException(e.getMessage(), e);
    }
  }

  @Override
  public List<String> listSchemas(DBNConnection connection) throws DatabaseOperationException {
    try {
      List<String> schemaList = executeCall(connection, new SchemasInfo(), "list-schemas").getSchemaList();
      return schemaList;
    } catch (SQLException e) {
      throw new DatabaseOperationException(e.getMessage(), e);
    }
  }

  @Override
  public List<DBObjectItem> listObjectListItems(DBNConnection connection, String schemaName) throws DatabaseOperationException {
    try {
      List<DBObjectItem> DBObjectItems = new ArrayList<>();
      // TODO : should be one roundtrip
      //    refactor later
      List<DBObjectItem> tableDBObjectListItems = executeCall(connection, new TableAndViewListInfo(schemaName, DatabaseObjectType.TABLE), "list-tables", schemaName).getDBObjectListItems();
      List<DBObjectItem> viewDBObjectListItems = executeCall(connection, new TableAndViewListInfo(schemaName, DatabaseObjectType.VIEW), "list-views", schemaName).getDBObjectListItems();
      List<DBObjectItem> materializedViewDBObjectListItems = executeCall(connection, new TableAndViewListInfo(schemaName, DatabaseObjectType.MATERIALIZED_VIEW), "list-materialized-views", schemaName).getDBObjectListItems();
      DBObjectItems.addAll(tableDBObjectListItems);
      DBObjectItems.addAll(viewDBObjectListItems);
      DBObjectItems.addAll(materializedViewDBObjectListItems);
      return DBObjectItems;
    } catch (SQLException e) {
      throw new DatabaseOperationException(e.getMessage(), e);
    }
  }

  @Override
  public void updateCredentialStatus(DBNConnection connection, String credentialName, boolean isEnabled) throws CredentialManagementException {
    try {
      executeCall(connection, null, isEnabled ? "enable-credential" : "disable-credential", credentialName);
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to disable credential: " + credentialName, e);
    }
  }

  @Override
  public void checkAdmin(DBNConnection connection) throws SQLException {
    executeCall(connection, null, "check-admin");
  }

  @Override
  public void grantPrivilege(DBNConnection connection, String username) throws SQLException {
    try {
      executeCall(connection, null, "grant-privilege", username);
    } catch (SQLException e) {
      throw new SQLException("Failed to grant privilege\n" + e.getMessage());
    }
  }

  @Override
  public void grantACLRights(DBNConnection connection, String command) throws SQLException {
    try {
      executeCall(connection, null, "acl-rights", command);
    } catch (SQLException e) {
      throw new SQLException("Failed to grant privilege\n" + e.getMessage());
    }
  }


}

