package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.Credential;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents detailed information about Oracle AI credentials, including associated profiles.
 * This class is designed to process the output of a callable statement that retrieves
 * credential information from an Oracle database.
 */
@Getter
public class OracleCredentialsDetailedInfo implements CallableStatementOutput {

  private List<Credential> credentialsProviders;

  private final String CREDENTIAL_NAME = "CREDENTIAL_NAME";
  private final String USERNAME = "USERNAME";
  private final String COMMENTS = "COMMENTS";
  private final String ENABLED = "ENABLED";


  /**
   * Registers parameters with the provided CallableStatement.
   * This method is meant to be implemented for statements that require parameter registration,
   * but it is not utilized in this class.
   *
   * @param statement The CallableStatement with which parameters are to be registered.
   * @throws SQLException If there is a database access error or other errors.
   */
  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    // Implementation not required for this class's context
  }

  /**
   * Reads and processes the output of a CallableStatement that executes a query
   * to retrieve credential information from the database. It builds a list of CredentialProvider
   * objects containing credentials and associated profiles.
   *
   * @param statement The CallableStatement from which to read the query output.
   * @throws SQLException If there is a database access error, the SQL statement is not a query,
   *                      or other SQL errors occur.
   */
  @Override
  public void read(CallableStatement statement) throws SQLException {
    ResultSet rs = statement.executeQuery();
    credentialsProviders = buildCredentialProviders(rs);
  }

  /**
   * Private helper method to construct a list of CredentialProvider objects from a ResultSet.
   * The method maps credential names to CredentialProvider objects and associates profiles
   * with their corresponding credentials.
   *
   * @param rs The ResultSet containing credential information from the database.
   * @return A list of CredentialProvider objects constructed from the ResultSet data.
   * @throws SQLException If an error occurs while accessing the ResultSet.
   */
  private List<Credential> buildCredentialProviders(ResultSet rs) throws SQLException {
    Map<String, Credential> credentialProviderBuildersMap = new HashMap<>();

    // Iterate over ResultSet to populate credentialProviderBuildersMap
    while (rs.next()) {
      String credentialName = rs.getString(CREDENTIAL_NAME);
      String username = rs.getString(USERNAME);
      String comments = rs.getString(COMMENTS);
      boolean enabled = rs.getBoolean(ENABLED);
      credentialProviderBuildersMap.computeIfAbsent(credentialName, k -> new Credential(credentialName, username, enabled, comments));
    }

    // Convert map values to a list and return
    return new ArrayList<>(credentialProviderBuildersMap.values());
  }
}
