package com.dci.intellij.dbn.execution.statement.variables.ui;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
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
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StatementExecutionInputForm extends DBNFormImpl<StatementExecutionInputsDialog> {
    private JPanel mainPanel;
    private JPanel variablesPanel;
    private JPanel previewPanel;
    private JPanel headerSeparatorPanel;
    private JPanel executionOptionsPanel;
    private JPanel headerPanel;
    private JPanel debuggerVersionPanel;
    private JLabel debuggerVersionLabel;
    private JLabel debuggerTypeLabel;

    private StatementExecutionProcessor executionProcessor;
    private List<StatementExecutionVariableValueForm> variableValueForms = new ArrayList<>();
    private ExecutionOptionsForm executionOptionsForm;
    private Document previewDocument;
    private EditorEx viewer;
    private String statementText;

    StatementExecutionInputForm(
            @NotNull StatementExecutionInputsDialog parentComponent,
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull DBDebuggerType debuggerType, boolean isBulkExecution) {
        super(parentComponent);
        this.executionProcessor = executionProcessor;
        this.statementText = executionProcessor.getExecutionInput().getExecutableStatementText();

        variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.Y_AXIS));
        headerSeparatorPanel.setBorder(Borders.BOTTOM_LINE_BORDER);
        headerSeparatorPanel.setVisible(false);

        ConnectionHandler connectionHandler = executionProcessor.getConnectionHandler();
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
        String headerTitle = psiFile.getName();
        Icon headerIcon = psiFile.getIcon();
        JBColor headerBackground = psiFile.getEnvironmentType().getColor();
        DBNHeaderForm headerForm = new DBNHeaderForm(headerTitle, headerIcon, headerBackground, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
        if (executionVariables != null) {
            List<StatementExecutionVariable> variables = new ArrayList<>(executionVariables.getVariables());
            variables.sort(StatementExecutionVariablesBundle.OFFSET_COMPARATOR);


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

            int[] metrics = new int[]{0, 0};
            for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
                metrics = variableValueForm.getMetrics(metrics);
            }

            for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
                variableValueForm.adjustMetrics(metrics);
            }
        } else {
            headerSeparatorPanel.setVisible(false);
        }

        executionOptionsForm = new ExecutionOptionsForm(this, executionProcessor.getExecutionInput(), debuggerType);
        this.executionOptionsPanel.add(executionOptionsForm.getComponent());

        updatePreview();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);

        JCheckBox reuseVariablesCheckBox = executionOptionsForm.getReuseVariablesCheckBox();
        if (isBulkExecution && executionVariables != null) {
            reuseVariablesCheckBox.setVisible(true);
            reuseVariablesCheckBox.addActionListener(e -> ensureParentComponent().setReuseVariables(reuseVariablesCheckBox.isSelected()));
        } else {
            reuseVariablesCheckBox.setVisible(false);
        }
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void disposeInner() {
        EditorUtil.releaseEditor(viewer);
        Disposer.dispose(variableValueForms);
        super.disposeInner();
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
        ConnectionHandler connectionHandler = Failsafe.nn(executionProcessor.getConnectionHandler());
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

            previewDocument = DocumentUtil.getDocument(selectStatementFile);

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

        } else {
            DocumentUtil.setText(previewDocument, previewText);
        }
    }
}
