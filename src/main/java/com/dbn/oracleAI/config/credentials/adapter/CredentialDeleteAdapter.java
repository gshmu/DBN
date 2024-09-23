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

package com.dbn.oracleAI.config.credentials.adapter;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.interfaces.DatabaseAssistantInterface;
import com.dbn.object.DBCredential;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.management.ObjectManagementAdapterBase;
import org.jetbrains.annotations.Nls;

import java.sql.SQLException;

/**
 * Implementation of the {@link com.dbn.object.management.ObjectManagementAdapter} specialized in deleting entities of type {@link DBCredential}
 * @author Dan Cioca (Oracle)
 */
public class CredentialDeleteAdapter extends ObjectManagementAdapterBase<DBCredential> {

    public CredentialDeleteAdapter(DBCredential credential) {
        super(credential, ObjectChangeAction.DELETE);
    }

    @Nls
    @Override
    protected String getProcessTitle() {
        return txt("prc.assistant.title.DeletingCredential");
    }

    @Nls
    @Override
    protected String getProcessDescription(DBCredential object) {
        return txt("prc.assistant.message.DeletingCredential", object.getType(), object.getName());
    }

    @Nls
    @Override
    protected String getSuccessMessage(DBCredential object) {
        return txt("msg.assistant.info.CredentialDeleteSuccess", object.getType(), object.getName());
    }

    @Nls
    @Override
    protected String getFailureMessage(DBCredential object) {
        return txt("msg.assistant.error.CredentialDeleteFailure", object.getType(), object.getName());
    }

    @Override
    protected void invokeDatabaseInterface(ConnectionHandler connection, DBNConnection conn, DBCredential credential) throws SQLException {
        DatabaseAssistantInterface assistantInterface = connection.getAssistantInterface();
        assistantInterface.deleteCredential(conn, credential.getName());
    }
}
