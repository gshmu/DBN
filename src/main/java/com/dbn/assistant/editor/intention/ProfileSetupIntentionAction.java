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

package com.dbn.assistant.editor.intention;

import com.dbn.assistant.DatabaseAssistantManager;
import com.dbn.assistant.editor.AssistantEditorUtil;
import com.dbn.code.common.intention.EditorIntentionAction;
import com.dbn.code.common.intention.EditorIntentionType;
import com.dbn.connection.ConnectionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import static com.dbn.assistant.editor.AssistantEditorUtil.isAssistantSupported;
import static com.dbn.assistant.editor.AssistantPromptUtil.isAssistantPromptAvailable;
import static com.dbn.common.dispose.Checks.isNotValid;

/**
 * Editor intention action allowing to switch the current profile
 *
 * @author Dan Cioca(Oracle)
 */
public class ProfileSetupIntentionAction extends EditorIntentionAction {
  @Override
  public EditorIntentionType getType() {
    return EditorIntentionType.ASSISTANT_PROFILE_SETUP;
  }

  public final String getText() {
    return "Select AI - Configure Profiles...";
  }

  @Override
  public final boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    return isAssistantSupported(editor) &&
            isAssistantPromptAvailable(editor, element);
  }

  @Override
  public final void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    ConnectionHandler connection = AssistantEditorUtil.getConnection(editor);
    if (isNotValid(connection)) return;

    DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);
    manager.openProfileConfiguration(connection);
  }
}
