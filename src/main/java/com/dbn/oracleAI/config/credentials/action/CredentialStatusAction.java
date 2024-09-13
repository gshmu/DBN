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

package com.dbn.oracleAI.config.credentials.action;

import com.dbn.object.DBCredential;
import com.dbn.oracleAI.config.credentials.CredentialManagementService;
import com.dbn.oracleAI.config.credentials.ui.CredentialManagementForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.intellij.icons.AllIcons.Diff.GutterCheckBox;
import static com.intellij.icons.AllIcons.Diff.GutterCheckBoxSelected;

/**
 * Toggle action for the credential management dialogs, allowing to quickly enable or disable a Credential
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class CredentialStatusAction extends CredentialManagementAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DBCredential credential = getSelectedCredential(e);
        if (credential == null) return;

        CredentialManagementService managementService = CredentialManagementService.getInstance(project);
        if (credential.isEnabled())
            managementService.disableCredential(credential, null); else
            managementService.enableCredential(credential, null);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        DBCredential credential = getSelectedCredential(e);
        boolean enabled = credential != null && credential.isEnabled();

        Presentation presentation = e.getPresentation();
        presentation.setIcon(enabled ? GutterCheckBoxSelected: GutterCheckBox);
        presentation.setText(enabled ? "Disable Credential" : "Enable Credential");
        presentation.setEnabled(isEnabled(e));
    }


    private static boolean isEnabled(@NotNull AnActionEvent e) {
        CredentialManagementForm managementForm = getManagementForm(e);
        if (managementForm == null) return false;
        if (managementForm.isLoading()) return false;
        if (managementForm.getSelectedCredential() == null) return false;

        return true;
    }
}
