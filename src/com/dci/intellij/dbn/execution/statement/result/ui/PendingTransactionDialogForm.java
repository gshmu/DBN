package com.dci.intellij.dbn.execution.statement.result.ui;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class PendingTransactionDialogForm extends DBNFormImpl<PendingTransactionDialog> {
    private JPanel mainPanel;
    private JPanel previewPanel;
    private JPanel headerPanel;
    private JTextPane hintTextPane;

    private StatementExecutionProcessor executionProcessor;
    private EditorEx viewer;

    public PendingTransactionDialogForm(final PendingTransactionDialog parentComponent, final StatementExecutionProcessor executionProcessor) {
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
        String headerName = psiFile.getName();
        Icon headerIcon = psiFile.getIcon();
        JBColor headerColor = psiFile.getEnvironmentType().getColor();
        DBNHeaderForm headerForm = new DBNHeaderForm(headerName, headerIcon, headerColor, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        updatePreview();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    @Override
    public void disposeInner() {
        EditorUtil.releaseEditor(viewer);
        super.disposeInner();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    private void updatePreview() {
        ConnectionHandler connectionHandler = Failsafe.nn(executionProcessor.getConnectionHandler());
        SchemaId currentSchema = executionProcessor.getTargetSchema();
        Project project = connectionHandler.getProject();
        String previewText = executionProcessor.getExecutionInput().getExecutableStatementText();

        DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE);
        DBLanguagePsiFile selectStatementFile = DBLanguagePsiFile.createFromText(
                project,
                "preview",
                languageDialect,
                previewText,
                connectionHandler,
                currentSchema);

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
