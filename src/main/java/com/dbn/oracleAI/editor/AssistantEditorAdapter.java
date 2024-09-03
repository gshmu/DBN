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

package com.dbn.oracleAI.editor;

import com.dbn.common.thread.Command;
import com.dbn.connection.ConnectionId;
import com.dbn.language.sql.SQLLanguage;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.DatabaseAssistantManager;
import com.dbn.oracleAI.model.ChatMessage;
import com.dbn.oracleAI.model.ChatMessageContext;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import lombok.experimental.UtilityClass;

/**
 * This is the class that handles processing the prompt from the console and displaying the results to it.
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
@UtilityClass
public class AssistantEditorAdapter {

  public static void submitQuery(Project project, Editor editor, ConnectionId connectionId, String prompt, ActionAIType action) {
    DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);
    prompt = prompt.trim();
    if (prompt.startsWith("---")) prompt = prompt.substring(3);

    AIProfileItem defaultProfile = manager.getDefaultProfile(connectionId);
    if (defaultProfile == null) {
      // TODO prompt profile creation wizard
      return;
    }
    ChatMessageContext context = new ChatMessageContext(defaultProfile.getName(), defaultProfile.getModel(), action);
    manager.generate(connectionId, prompt, context, message -> appendMessage(project, editor, message));
  }

  private static void appendMessage(Project project, Editor editor, ChatMessage message) {
    Command.run(project, "Database Assistant Response", () -> {
      int caretLine = editor.getCaretModel().getCurrentCaret().getLogicalPosition().line;
      Document document = editor.getDocument();

      int offset = document.getLineEndOffset(caretLine);
      String content = message.outputForLanguage(SQLLanguage.INSTANCE);
      document.insertString(offset, "\n" + content);
    });
  }


  private static void appendLine(Project project, Document document, String lineToAppend, String afterComment) {
    ApplicationManager.getApplication().invokeLater(() -> {
      WriteCommandAction.runWriteCommandAction(project, () -> {
        String content = document.getText();
        int pos = content.indexOf(afterComment);

        if (pos >= 0) {
          pos = content.indexOf("\n", pos);
          if (pos >= 0) {
            document.insertString(pos + 1, lineToAppend + "\n");
          } else {
            document.insertString(content.length(), "\n" + lineToAppend + "\n");
          }
        } else {
          document.insertString(document.getTextLength(), "\n" + lineToAppend);
        }
      });
    });
  }

  private static String processText(String input, boolean withExplanation) {
    StringBuilder result = new StringBuilder();
    boolean inCodeBlock = false;
    boolean inExplanationBlock = false;
    String[] lines = input.split("\n");
    result.append("\n");

    for (String line : lines) {
      if (line.trim().startsWith("```")) {
        if (!inCodeBlock && inExplanationBlock) {
          result.append("*/\n\n");
          inExplanationBlock = false;
        }
        inCodeBlock = !inCodeBlock;
        continue;
      }

      if (inCodeBlock) {
        result.append(line).append("\n");
      } else {
        if (!inExplanationBlock && withExplanation) {
          result.append("\n/*\n");
          inExplanationBlock = true;
        }
        result.append(line).append("\n");
      }
    }

    if (inExplanationBlock) {
      result.append("*/\n\n");
    }

    return result.toString();
  }

}
