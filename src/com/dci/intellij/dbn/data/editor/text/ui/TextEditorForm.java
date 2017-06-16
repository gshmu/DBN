package com.dci.intellij.dbn.data.editor.text.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
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
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.sql.SQLException;

public class TextEditorForm extends DBNFormImpl<TextEditorDialog> {
    private JPanel mainPanel;
    private JPanel editorPanel;
    private JPanel actionsPanel;

    private EditorEx editor;
    private UserValueHolder userValueHolder;
    private String error;
    private String text;

    private TextEditorAdapter textEditorAdapter;
    private DocumentListener documentListener;


    public JComponent getComponent() {
        return mainPanel;
    }

    public TextEditorForm(TextEditorDialog parent, DocumentListener documentListener, UserValueHolder userValueHolder, TextEditorAdapter textEditorAdapter) throws SQLException {
        super(parent);
        this.documentListener = documentListener;
        this.userValueHolder = userValueHolder;
        this.textEditorAdapter = textEditorAdapter;

        Project project = getProject();
        if (userValueHolder.getContentType() == null) {
            userValueHolder.setContentType(TextContentType.getPlainText(project));
        }

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.LobContentTypeEditor", true,
                new TextContentTypeComboBoxAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        text = StringUtil.removeCharacter(CommonUtil.nvl(readUserValue(), ""), '\r');
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

        Project project = getProject();
        FileType fileType = userValueHolder.getContentType().getFileType();
        if (fileType instanceof LanguageFileType) {
            LanguageFileType languageFileType = (LanguageFileType) fileType;

            VirtualFile virtualFile = new LightVirtualFile("text_editor_file", fileType, text);

            FileManager fileManager = ((PsiManagerEx)PsiManager.getInstance(project)).getFileManager();
            FileViewProvider viewProvider = fileManager.createFileViewProvider(virtualFile, true);
            PsiFile psiFile = viewProvider.getPsi(languageFileType.getLanguage());
            document = psiFile == null ? null : DocumentUtil.getDocument(psiFile);
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
            EditorFactory.getInstance().releaseEditor(oldEditor);

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
    public String readUserValue() throws SQLException {
        Object userValue = userValueHolder.getUserValue();
        if (userValue instanceof String) {
            return (String) userValue;
        } else if (userValue instanceof LargeObjectValue) {
            LargeObjectValue largeObjectValue = (LargeObjectValue) userValue;
            return largeObjectValue.read();
        }
        return null;
    }

    public void writeUserValue() throws SQLException {
        String text = editor.getDocument().getText();
        userValueHolder.updateUserValue(text, false);
        textEditorAdapter.afterUpdate();
    }

    public TextContentType getContentType() {
        return userValueHolder.getContentType();
    }

    public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
        editor = null;
        documentListener = null;
        //editor = null;

    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return editor.getContentComponent();
    }
}
