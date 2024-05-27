package com.dbn.oracleAI;


import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * AI profile credential service
 */
public interface AICredentialService {
  /**
   * Asynchronously creates a new credential.
   *
   * @throws CredentialManagementException underlying service failed
   */
  CompletableFuture<Void> createCredential(Credential credential);

  /**
   * Asynchronously updates an attributes of existing credential.
   *
   * @throws CredentialManagementException
   */
  CompletableFuture<Void> updateCredential(Credential editedCredential);

  /**
   * Asynchronously lists detailed credential information from the database.
   *
   * @throws CredentialManagementException
   */
  CompletableFuture<List<Credential>> getCredentials();

  /**
   * Asynchronously deletes a specific credential information from the database.
   *
   * @throws CredentialManagementException
   */
  CompletableFuture<Void> deleteCredential(String credentialName);

  /**
   * Asynchronously updates the status (enabled/disabled) of the credential from the database.
   */
  void updateStatus(String credentialName, Boolean isEnabled);
}
