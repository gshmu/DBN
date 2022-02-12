package com.dci.intellij.dbn.execution.statement.variables.ui;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.common.ui.ExecutionOptionsForm;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariable;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StatementExecutionInputForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel variablesPanel;
    private JPanel executionOptionsPanel;
    private JPanel headerPanel;
    private JPanel debuggerVersionPanel;
    private JLabel debuggerVersionLabel;
    private JLabel debuggerTypeLabel;
    private JPanel splitPreviewPanel;
    private JPanel previewPanel;
    private JPanel splitPanel;
    private JSplitPane splitPane;
    private JScrollPane variablesScrollPane;

    private StatementExecutionProcessor executionProcessor;
    private final List<StatementExecutionVariableValueForm> variableValueForms = DisposableContainer.list(this);
    private final ExecutionOptionsForm executionOptionsForm;
    private final String statementText;
    private Document previewDocument;
    private EditorEx viewer;

    StatementExecutionInputForm(
            @NotNull StatementExecutionInputsDialog parent,
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull DBDebuggerType debuggerType, boolean isBulkExecution) {
        super(parent);
        this.executionProcessor = executionProcessor;
        this.statementText = executionProcessor.getExecutionInput().getExecutableStatementText();

        variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.Y_AXIS));

        ConnectionHandler connectionHandler = executionProcessor.getConnection();
        if (debuggerType.isDebug()) {
            debuggerVersionPanel.setVisible(true);
            debuggerVersionPanel.setBorder(Borders.BOTTOM_LINE_BORDER);
            DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
            String debuggerVersion = debuggerManager.getDebuggerVersion(connectionHandler);
            debuggerVersionLabel.setText(debuggerVersion);
            debuggerTypeLabel.setText(debuggerType.name());
        } else {
            debuggerVersionPanel.setVisible(false);
        }

        DBLanguagePsiFile psiFile = executionProcessor.getPsiFile();
        String headerTitle = executionProcessor.getName();
        Icon headerIcon = executionProcessor.getIcon();
        JBColor headerBackground = psiFile == null ?
                EnvironmentType.DEFAULT.getColor() :
                psiFile.getEnvironmentType().getColor();

        DBNHeaderForm headerForm = new DBNHeaderForm(this, headerTitle, headerIcon, headerBackground);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
        if (executionVariables != null) {
            mainPanel.remove(previewPanel);

            List<StatementExecutionVariable> variables = new ArrayList<>(executionVariables.getVariables());
            variables.sort(StatementExecutionVariablesBundle.NAME_COMPARATOR);


            for (StatementExecutionVariable variable: variables) {
                StatementExecutionVariableValueForm variableValueForm = new StatementExecutionVariableValueForm(this, variable);
                variableValueForms.add(variableValueForm);
                variablesPanel.add(variableValueForm.getComponent());
                variableValueForm.addDocumentListener(new DocumentAdapter() {
                    @Override
                    protected void textChanged(@NotNull DocumentEvent e) {
                        updatePreview();
                    }
                });
            }
            splitPane.setDividerLocation((int)variablesPanel.getPreferredSize().getHeight());
            Dimension preferredSize = variablesScrollPane.getPreferredSize();
            preferredSize.setSize(preferredSize.getWidth() + 20, preferredSize.getHeight());
            variablesScrollPane.setPreferredSize(preferredSize);

            int[] metrics = new int[]{0, 0};
            for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
                metrics = variableValueForm.getMetrics(metrics);
            }

            for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
                variableValueForm.adjustMetrics(metrics);
            }
        } else {
            mainPanel.remove(splitPanel);
        }

        executionOptionsForm = new ExecutionOptionsForm(this, executionProcessor.getExecutionInput(), debuggerType);
        executionOptionsPanel.add(executionOptionsForm.getComponent());

        updatePreview();

        JCheckBox reuseVariablesCheckBox = executionOptionsForm.getReuseVariablesCheckBox();
        if (isBulkExecution && executionVariables != null) {
            reuseVariablesCheckBox.setVisible(true);
            reuseVariablesCheckBox.addActionListener(e -> getParentDialog().setReuseVariables(reuseVariablesCheckBox.isSelected()));
        } else {
            reuseVariablesCheckBox.setVisible(false);
        }
    }

    @NotNull
    public StatementExecutionInputsDialog getParentDialog() {
        return (StatementExecutionInputsDialog) ensureParent();
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        if (variableValueForms.size() > 0) {
            return variableValueForms.get(0).getEditorComponent();
        }
        return null;
    }

    public void updateExecutionInput() {
        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            variableValueForm.saveValue();
        }
        executionOptionsForm.updateExecutionInput();
    }

    void updatePreview() {
        ConnectionHandler connectionHandler = Failsafe.nn(executionProcessor.getConnection());
        SchemaId currentSchema = executionProcessor.getTargetSchema();
        Project project = connectionHandler.getProject();
        String previewText = this.statementText;

        StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
        if (executionVariables != null) {
            previewText = executionVariables.prepareStatementText(connectionHandler, this.statementText, true);

            for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
                String errorText = executionVariables.getError(variableValueForm.getVariable());
                if (errorText == null)
                    variableValueForm.hideErrorLabel(); else
                    variableValueForm.showErrorLabel(errorText);
            }
        }


        if (previewDocument == null) {
            DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE);
            DBLanguagePsiFile selectStatementFile = DBLanguagePsiFile.createFromText(
                    project,
                    "preview",
                    languageDialect,
                    previewText,
                    connectionHandler,
                    currentSchema);

            previewDocument = Documents.getDocument(selectStatementFile);

            viewer = (EditorEx) EditorFactory.getInstance().createViewer(previewDocument, project);
            viewer.setEmbeddedIntoDialogWrapper(true);
            Editors.initEditorHighlighter(viewer, SQLLanguage.INSTANCE, connectionHandler);
            Editors.setEditorReadonly(viewer, true);

            JScrollPane viewerScrollPane = viewer.getScrollPane();
            viewerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            viewerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            //viewerScrollPane.setBorder(null);
            viewerScrollPane.setViewportBorder(Borders.lineBorder(Colors.getReadonlyEditorBackground(), 4));

            EditorSettings settings = viewer.getSettings();
            settings.setFoldingOutlineShown(false);
            settings.setLineMarkerAreaShown(false);
            settings.setLineNumbersShown(false);
            settings.setVirtualSpace(false);
            settings.setDndEnabled(false);
            settings.setAdditionalLinesCount(2);
            settings.setRightMarginShown(false);
            JComponent viewerComponent = viewer.getComponent();
            if (executionVariables == null)
                previewPanel.add(viewerComponent, BorderLayout.CENTER); else
                splitPreviewPanel.add(viewerComponent, BorderLayout.CENTER);

        } else {
            Documents.setText(previewDocument, previewText);
        }
    }


    @Override
    public void disposeInner() {
        Editors.releaseEditor(viewer);
        executionProcessor = null;
        super.disposeInner();
    }
}
