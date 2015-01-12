package com.dci.intellij.dbn.execution.method.result.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.editor.data.options.DataEditorQualifiedEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.lookup.DBArgumentRef;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;

public class MethodExecutionLargeValueResultForm extends DBNFormImpl implements DBNForm {
    private JPanel actionsPanel;
    private JPanel mainPanel;
    private JPanel largeValuePanel;
    private DBArgumentRef argumentRef;

    private EditorEx editor;
    private TextContentType contentType;

    public MethodExecutionLargeValueResultForm(MethodExecutionResult executionResult, DBArgument argument) {
        argumentRef = argument.getRef();
        Project project = argument.getProject();

        ArgumentValue argumentValue = executionResult.getArgumentValue(argumentRef);
        String text = (String) argumentValue.getValue();

        Document document = EditorFactory.getInstance().createDocument(text == null ? "" : StringUtil.removeCharacter(text, '\r'));
        contentType = TextContentType.get(project, argument.getDataType().getContentTypeName());
        if (contentType == null) contentType = TextContentType.getPlainText(project);

        editor = (EditorEx) EditorFactory.getInstance().createEditor(document, project, contentType.getFileType(), false);
        editor.getContentComponent().setFocusTraversalKeysEnabled(false);

        largeValuePanel.add(editor.getComponent(), BorderLayout.CENTER);


        largeValuePanel.setBorder(IdeBorderFactory.createBorder());

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.MethodExecutionResult.LobContentTypeEditor", true,
                new ContentTypeComboBoxAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);


/*
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new CursorResultFetchNextRecordsAction(executionResult, resultTable),
                new CursorResultViewRecordAction(resultTable),
                ActionUtil.SEPARATOR,
                new CursorResultExportAction(resultTable, argument));

        actionsPanel.add(actionToolbar.getComponent());
*/
    }

    public void setContentType(TextContentType contentType) {
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(contentType.getFileType(), editor.getProject(), null);
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        editor.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
    }

    public DBArgument getArgument() {
        return argumentRef.get();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public class ContentTypeComboBoxAction extends DBNComboBoxAction {

        public ContentTypeComboBoxAction() {
            Presentation presentation = getTemplatePresentation();
            presentation.setText(contentType.getName());
            presentation.setIcon(contentType.getIcon());
        }

        @NotNull
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            Project project = ActionUtil.getProject(button);
            DataEditorQualifiedEditorSettings qualifiedEditorSettings = DataEditorSettings.getInstance(project).getQualifiedEditorSettings();

            DefaultActionGroup actionGroup = new DefaultActionGroup();
            for (TextContentType contentType : qualifiedEditorSettings.getContentTypes()) {
                if (contentType.isSelected()) {
                    actionGroup.add(new ContentTypeSelectAction(contentType));
                }

            }
            return actionGroup;
        }

        @Override
        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText(contentType.getName());
            presentation.setIcon(contentType.getIcon());
        }
    }

    public class ContentTypeSelectAction extends AnAction {
        private TextContentType contentType;

        public ContentTypeSelectAction(TextContentType contentType) {
            super(contentType.getName(), null, contentType.getIcon());
            this.contentType = contentType;
        }

        public TextContentType getContentType() {
            return contentType;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.getProject(e);
            SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(contentType.getFileType(), project, null);
            EditorColorsScheme colorsScheme = editor.getColorsScheme();
            editor.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
            MethodExecutionLargeValueResultForm.this.contentType = contentType;
        }
    }

    public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
    }
}
