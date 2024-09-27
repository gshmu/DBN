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

package com.dbn.assistant.chat.window.ui;

import com.dbn.common.color.Colors;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.ref.WeakRef;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.common.util.Documents;
import com.dbn.common.util.Editors;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Input field used for the chat-box user prompt
 *
 * @author Dan Cioca (Oracle)
 */
public class ChatBoxInputField extends JPanel implements Disposable {
    private final EditorEx editor;
    private final WeakRef<ChatBoxForm> chatBox;


    public ChatBoxInputField(ChatBoxForm chatBox) {
        super(new BorderLayout());
        this.chatBox = WeakRef.of(chatBox);
        this.editor = createEditor();
        add(editor.getComponent(), BorderLayout.CENTER);
        Disposer.register(chatBox, this);
    }

    @Override
    public void requestFocus() {
        editor.getContentComponent().requestFocus();
    }

    public String getText() {
        return editor.getDocument().getText();
    }

    private ChatBoxForm getChatBox() {
        return chatBox.ensure();
    }

    public void setText(String text) {
        editor.getDocument().setText(text);
    }

    public String getAndClearText() {
        DocumentEx document = editor.getDocument();
        String text = document.getText().trim();
        Documents.setText(document, "");
        return text;
    }

    public void setReadonly(boolean readonly) {
        Editors.setEditorReadonly(editor, readonly);
        UserInterface.repaint(this);
    }

    private EditorEx createEditor() {
        PlainTextLanguage language = PlainTextLanguage.INSTANCE;
        VirtualFile file = new LightVirtualFile("prompt.txt", language, "");

        Project project = getChatBox().getProject();
        PsiManagerEx psiManager = (PsiManagerEx) PsiManager.getInstance(project);
        FileManager fileManager = psiManager.getFileManager();
        FileViewProvider viewProvider = fileManager.createFileViewProvider(file, true);
        PsiFile psiFile = viewProvider.getPsi(language);
        Document document = Documents.ensureDocument(psiFile);

        EditorEx editor = Editors.createEditor(document, project, file, file.getFileType());
        editor.setEmbeddedIntoDialogWrapper(false);

        JScrollPane scrollPane = editor.getScrollPane();
        Color backgroundColor = Colors.delegate(editor::getBackgroundColor);
        scrollPane.setViewportBorder(Borders.lineBorder(backgroundColor, 2, 4, 2, 4) );
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        document.addDocumentListener(new EnterKeyInterceptor());

        EditorSettings settings = editor.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(1);
        settings.setRightMarginShown(false);
        settings.setCaretRowShown(false);
        settings.setUseSoftWraps(true);
        settings.setAdditionalLinesCount(2);

        return editor;

    }

    private class EnterKeyInterceptor implements DocumentListener {
        @Override
        public void beforeDocumentChange(@NotNull DocumentEvent event) {
            Document document = event.getDocument();
            int textLength = document.getTextLength();
            if (event.getOffset() != textLength) return;

            CharSequence newFragment = event.getNewFragment();
            if (!newFragment.isEmpty() && newFragment.charAt(0) == '\n') {
                ChatBoxForm chatBox = getChatBox();
                if (chatBox.getAssistantState().isPromptingAvailable()) {
                    chatBox.submitPrompt();
                } else {
                    String text = document.getText();
                    Documents.setText(document, text.trim());
                }
            }
        }
    }

    @Override
    public void dispose() {
        Editors.releaseEditor(editor);
    }
}
