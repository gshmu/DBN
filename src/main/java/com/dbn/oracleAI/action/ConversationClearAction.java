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

package com.dbn.oracleAI.action;

import com.dbn.common.action.DataKeys;
import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.oracleAI.ui.ChatBoxForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Action for clearing the AI Assistant conversation history
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class ConversationClearAction extends ProjectAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ChatBoxForm chatBox = e.getData(DataKeys.COMPANION_CHAT_BOX);
        if (chatBox == null) return;

        chatBox.clearConversation();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ChatBoxForm chatBox = e.getData(DataKeys.COMPANION_CHAT_BOX);
        boolean enabled = chatBox != null && !chatBox.getState().getMessages().isEmpty();

        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.ACTION_DELETE);
        presentation.setText("Clear Conversation");
        presentation.setDescription("Clear all conversation messages");
        presentation.setEnabled(enabled);
    }
}
