package com.dci.intellij.dbn.data.editor.text.ui;

import com.dci.intellij.dbn.common.action.UserDataKeys;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.util.*;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.editor.text.TextEditorAdapter;
import com.dci.intellij.dbn.data.editor.text.actions.TextContentTypeComboBoxAction;
import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class TextEditorForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel editorPanel;
    private JPanel actionsPanel;

    private EditorEx editor;
    private String error;
    private String text;

    private final UserValueHolder<?> userValueHolder;
    private final TextEditorAdapter textEditorAdapter;
    private final DocumentListener documentListener;


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public TextEditorForm(TextEditorDialog parent, DocumentListener documentListener, UserValueHolder<?> userValueHolder, TextEditorAdapter textEditorAdapter) {
        super(parent);
        this.documentListener = documentListener;
        this.userValueHolder = userValueHolder;
        this.textEditorAdapter = textEditorAdapter;

        Project project = getProject();
        if (userValueHolder.getContentType() == null) {
            userValueHolder.setContentType(TextContentType.getPlainText(project));
        }

        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,
                "DBNavigator.Place.DataEditor.LobContentTypeEditor", true,
                new TextContentTypeComboBoxAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        text = Strings.removeCharacter(Commons.nvl(readUserValue(), ""), '\r');
        initEditor();
    }

    private void initEditor() {
        Document document = null;
        EditorEx oldEditor = editor;
        if (oldEditor != null) {
            document = oldEditor.getDocument();
            document.removeDocumentListener(documentListener);
            text = document.getText();
            document = null;
        }

        Project project = ensureProject();
        FileType fileType = userValueHolder.getContentType().getFileType();
        if (fileType instanceof LanguageFileType) {
            LanguageFileType languageFileType = (LanguageFileType) fileType;

            VirtualFile virtualFile = new LightVirtualFile("text_editor_file", fileType, text);
            virtualFile.putUserData(UserDataKeys.HAS_CONNECTIVITY_CONTEXT, false);

            FileManager fileManager = ((PsiManagerEx)PsiManager.getInstance(project)).getFileManager();
            FileViewProvider viewProvider = fileManager.createFileViewProvider(virtualFile, true);
            PsiFile psiFile = viewProvider.getPsi(languageFileType.getLanguage());
            document = psiFile == null ? null : Documents.getDocument(psiFile);
        }

        if (document == null) {
            document = EditorFactory.getInstance().createDocument(text);
        }

        document.addDocumentListener(documentListener);
        editor = (EditorEx) EditorFactory.getInstance().createEditor(document, project, fileType, false);
        editor.setEmbeddedIntoDialogWrapper(true);
        editor.getContentComponent().setFocusTraversalKeysEnabled(false);

        if (oldEditor!= null) {
            editorPanel.remove(oldEditor.getComponent());
            Editors.releaseEditor(oldEditor);

        }
        editorPanel.add(editor.getComponent(), BorderLayout.CENTER);
    }

    public void setContentType(TextContentType contentType){
        if (userValueHolder.getContentType() != contentType) {
            userValueHolder.setContentType(contentType);
            initEditor();
        }

/*
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(contentType.getFileType(), userValueHolder.getProject(), null);
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        editor.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
*/


    }

    @Nullable
    public String readUserValue() {
        try {
            Object userValue = userValueHolder.getUserValue();
            if (userValue instanceof String) {
                return (String) userValue;
            } else if (userValue instanceof LargeObjectValue) {
                LargeObjectValue largeObjectValue = (LargeObjectValue) userValue;
                return largeObjectValue.read();
            }
        } catch (SQLException e) {
            Messages.showErrorDialog(getProject(), "Could not load LOB content from database.", e);
        }
        return null;
    }

    @NotNull
    public String getText() {
        return editor.getDocument().getText();
    }

    public TextContentType getContentType() {
        return userValueHolder.getContentType();
    }

    @Override
    public void disposeInner() {
        Editors.releaseEditor(editor);
        super.disposeInner();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return editor.getContentComponent();
    }
}
