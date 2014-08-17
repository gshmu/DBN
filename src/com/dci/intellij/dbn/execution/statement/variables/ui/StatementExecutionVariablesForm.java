package com.dci.intellij.dbn.execution.statement.variables.ui;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariable;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.sql.SQLFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.GuiUtils;

public class StatementExecutionVariablesForm extends DBNFormImpl implements DBNForm {
    private List<StatementExecutionVariableValueForm> variableValueForms = new ArrayList<StatementExecutionVariableValueForm>();
    private StatementExecutionVariablesBundle variablesBundle;
    private JPanel mainPanel;
    private JPanel variablesPanel;
    private JPanel previewPanel;
    private JPanel headerSeparatorPanel;
    private Document previewDocument;
    private EditorEx viewer;
    private String statementText;

    public StatementExecutionVariablesForm(StatementExecutionVariablesBundle variablesBundle, String statementText) {
        this.variablesBundle = variablesBundle;
        this.statementText = statementText;

        variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.Y_AXIS));
        headerSeparatorPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        for (StatementExecutionVariable variable: variablesBundle.getVariables()) {
            StatementExecutionVariableValueForm variableValueForm = new StatementExecutionVariableValueForm(variable);
            variableValueForms.add(variableValueForm);
            variablesPanel.add(variableValueForm.getComponent());
            variableValueForm.addDocumentListener(new DocumentAdapter() {
                protected void textChanged(DocumentEvent e) {
                    updatePreview();
                }
            });
            variableValueForm.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
        updatePreview();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            variableValueForm.dispose();
        }
        variableValueForms.clear();
        variablesBundle = null;
        EditorFactory.getInstance().releaseEditor(viewer);
    }

    public JComponent getPreferredFocusComponent() {
        if (variableValueForms.size() > 0) {
            return variableValueForms.get(0).getEditorComponent();
        }
        return null;
    }

    public void saveValues() {
        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            variableValueForm.saveValue();
        }
    }

    private void updatePreview() {
        ConnectionHandler connectionHandler = variablesBundle.getActiveConnection();
        Project project = connectionHandler.getProject();

        String previewText = variablesBundle.prepareStatementText(connectionHandler, this.statementText, true);

        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            String errorText = variablesBundle.getError(variableValueForm.getVariable());
            if (errorText == null)
                variableValueForm.hideErrorLabel(); else
                variableValueForm.showErrorLabel(errorText);
        }

        if (previewDocument == null) {
            PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);

            SQLFile selectStatementFile = (SQLFile)
                psiFileFactory.createFileFromText(
                    "filter.sql",
                    connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE),
                    previewText);

            selectStatementFile.setActiveConnection(connectionHandler);
            selectStatementFile.setCurrentSchema(variablesBundle.getCurrentSchema());
            previewDocument = DocumentUtil.getDocument(selectStatementFile);

            viewer = (EditorEx) EditorFactory.getInstance().createViewer(previewDocument, project);
            viewer.setEmbeddedIntoDialogWrapper(true);
            JScrollPane viewerScrollPane = viewer.getScrollPane();
            SyntaxHighlighter syntaxHighlighter = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE).getSyntaxHighlighter();
            EditorColorsScheme colorsScheme = viewer.getColorsScheme();
            viewer.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
            viewer.setBackgroundColor(colorsScheme.getColor(ColorKey.find("CARET_ROW_COLOR")));
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
            final String finalPreviewText = previewText;

            new WriteActionRunner() {
                public void run() {
                    previewDocument.setText(finalPreviewText);
                }
            }.start();
        }
    }
}
