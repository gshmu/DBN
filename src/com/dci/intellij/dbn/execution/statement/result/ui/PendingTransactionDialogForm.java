package com.dci.intellij.dbn.execution.statement.result.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;

public class PendingTransactionDialogForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel previewPanel;
    private JPanel headerPanel;
    private JTextPane hintTextPane;

    private final WeakRef<StatementExecutionProcessor> executionProcessor;
    private EditorEx viewer;

    public PendingTransactionDialogForm(PendingTransactionDialog parent, final StatementExecutionProcessor executionProcessor) {
        super(parent);
        this.executionProcessor = WeakRef.of(executionProcessor);

        String text =
                "You executed this statement in a pool connection. \n" +
                "The transactional status of this connection cannot be left inconsistent. Please choose whether to commit or rollback the changes.\n\n" +
                "NOTE: Changes will be rolled-back if this prompt stays unattended for more than 5 minutes";
        hintTextPane.setBackground(mainPanel.getBackground());
        hintTextPane.setFont(mainPanel.getFont());
        hintTextPane.setText(text);

        String headerName = executionProcessor.getName();
        Icon headerIcon = executionProcessor.getIcon();

        DBLanguagePsiFile psiFile = executionProcessor.getPsiFile();
        JBColor headerColor = psiFile == null ?
                EnvironmentType.DEFAULT.getColor() :
                psiFile.getEnvironmentType().getColor();
        DBNHeaderForm headerForm = new DBNHeaderForm(this, headerName, headerIcon, headerColor);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        updatePreview();
    }

    @NotNull
    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor.ensure();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    private void updatePreview() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor();

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

        if (selectStatementFile != null) {
            Document previewDocument = Documents.getDocument(selectStatementFile);

            viewer = (EditorEx) EditorFactory.getInstance().createViewer(previewDocument, project);
            viewer.setEmbeddedIntoDialogWrapper(true);
            JScrollPane viewerScrollPane = viewer.getScrollPane();

            Editors.initEditorHighlighter(viewer, SQLLanguage.INSTANCE, connectionHandler);
            viewer.setBackgroundColor(Colors.stronger(viewer.getBackgroundColor(), 1));
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


    @Override
    public void disposeInner() {
        Editors.releaseEditor(viewer);
        viewer = null;

        super.disposeInner();
    }
}
