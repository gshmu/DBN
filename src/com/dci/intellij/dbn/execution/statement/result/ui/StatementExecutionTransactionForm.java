package com.dci.intellij.dbn.execution.statement.result.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.ui.GuiUtils;

public class StatementExecutionTransactionForm extends DBNFormImpl<StatementExecutionTransactionDialog> {
    private JPanel mainPanel;
    private JPanel previewPanel;
    private JPanel headerPanel;
    private JTextPane hintTextPane;

    private StatementExecutionProcessor executionProcessor;
    private EditorEx viewer;

    public StatementExecutionTransactionForm(final StatementExecutionTransactionDialog parentComponent, final StatementExecutionProcessor executionProcessor) {
        super(parentComponent);
        this.executionProcessor = executionProcessor;

        String text =
                "You executed this statement in a pool connection. \n" +
                "The transactional status of this connection cannot be left inconsistent. Please choose whether to commit or rollback the changes.\n\n" +
                "NOTE: Changes will be rolled-back if this prompt stays unattended for more than 5 minutes";
        hintTextPane.setBackground(mainPanel.getBackground());
        hintTextPane.setFont(mainPanel.getFont());
        hintTextPane.setText(text);

        DBLanguagePsiFile psiFile = executionProcessor.getPsiFile();
        DBNHeaderForm headerForm = new DBNHeaderForm(psiFile.getName(), psiFile.getIcon(), psiFile.getEnvironmentType().getColor());
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        updatePreview();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        executionProcessor = null;
        EditorFactory.getInstance().releaseEditor(viewer);
    }

    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    private void updatePreview() {
        ConnectionHandler connectionHandler = FailsafeUtil.get(executionProcessor.getConnectionHandler());
        DBSchema currentSchema = executionProcessor.getTargetSchema();
        Project project = connectionHandler.getProject();
        String previewText = executionProcessor.getExecutionInput().getExecutableStatementText();

        DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE);
        DBLanguagePsiFile selectStatementFile = DBLanguagePsiFile.createFromText(project, "preview", languageDialect, previewText, connectionHandler, currentSchema);
        Document previewDocument = DocumentUtil.getDocument(selectStatementFile);

        viewer = (EditorEx) EditorFactory.getInstance().createViewer(previewDocument, project);
        viewer.setEmbeddedIntoDialogWrapper(true);
        JScrollPane viewerScrollPane = viewer.getScrollPane();
        SyntaxHighlighter syntaxHighlighter = languageDialect.getSyntaxHighlighter();
        EditorColorsScheme colorsScheme = viewer.getColorsScheme();
        viewer.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
        viewer.setBackgroundColor(GUIUtil.adjustColor(viewer.getBackgroundColor(), -0.01));
        viewerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        viewerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //viewerScrollPane.setBorder(null);
        viewerScrollPane.setViewportBorder(new LineBorder(CompatibilityUtil.getEditorBackgroundColor(viewer), 4, false));

        EditorSettings settings = viewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);
        previewPanel.add(viewer.getComponent(), BorderLayout.CENTER);
    }
}
