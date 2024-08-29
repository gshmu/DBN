/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI.service.mock;

import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.service.AICredentialService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Mockup credential service.
 * This service use data from JSON dump files.
 * Default location are
 *     /var/tmp/credentials.json
 * Location can be overided by following system properties
 *  fake.services.credential.dump
 *
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 */
public class FakeAICredentialService implements AICredentialService {

  Type CREDENTIAL_TYPE = new TypeToken<List<Credential>>() {
  }.getType();
  String credentialsRepoFilename = System.getProperty("fake.services.credential.dump", "/var/tmp/credentials.json");
  //keep track of profiles
  // no synch needed
  List<Credential> credentials = null;


  @Override
  public CompletableFuture<Void> createCredential(Credential credential) {
    this.credentials.add(credential);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> updateCredential(Credential credential) {
    this.credentials.removeIf(cred -> cred.getCredentialName().equalsIgnoreCase(credential.getCredentialName()));
    this.credentials.add(credential);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<List<Credential>> getCredentials() {
    if (credentials == null) {
      try {
        this.credentials = new Gson().fromJson(new FileReader(credentialsRepoFilename), CREDENTIAL_TYPE);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("cannot read credentials  list " + e.getMessage());
      }
    }
    return CompletableFuture.completedFuture(this.credentials);
  }

  @Override
  public CompletableFuture<Void> deleteCredential(String credentialName) {
    this.credentials.removeIf(cred -> cred.getCredentialName().equalsIgnoreCase(credentialName));
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void updateStatus(String credentialName, Boolean isEnabled) {
  }
}
