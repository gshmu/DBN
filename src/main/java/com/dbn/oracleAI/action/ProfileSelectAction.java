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
import com.dbn.common.util.Actions;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.ui.ChatBoxForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Action for selecting one individual AI-assistant profile
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class ProfileSelectAction extends ProjectAction {
    private final AIProfileItem profile;
    ProfileSelectAction(AIProfileItem profile) {
        this.profile = profile;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ChatBoxForm chatBox = e.getData(DataKeys.COMPANION_CHAT_BOX);
        if (chatBox == null) return;

        chatBox.selectProfile(profile);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText(Actions.adjustActionName(profile.getName()));
        presentation.setEnabled(profile.isEnabled());
    }
}
