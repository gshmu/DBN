package com.dci.intellij.dbn.execution.method.result.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.lookup.DBArgumentRef;
import com.intellij.ide.highlighter.HighlighterFactory;
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

    public MethodExecutionLargeValueResultForm(MethodExecutionResult executionResult, DBArgument argument) {
        argumentRef = argument.getRef();
        Project project = argument.getProject();

        ArgumentValue argumentValue = executionResult.getArgumentValue(argumentRef);
        String text = (String) argumentValue.getValue();

        Document document = EditorFactory.getInstance().createDocument(text == null ? "" : StringUtil.removeCharacter(text, '\r'));
        TextContentType contentType = TextContentType.get(project, argument.getDataType().getContentTypeName());
        if (contentType == null) contentType = TextContentType.getPlainText(project);

        editor = (EditorEx) EditorFactory.getInstance().createEditor(document, project, contentType.getFileType(), false);
        editor.getContentComponent().setFocusTraversalKeysEnabled(false);

        largeValuePanel.add(editor.getComponent(), BorderLayout.CENTER);


        largeValuePanel.setBorder(IdeBorderFactory.createBorder());

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

    public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
    }
}
