package com.dci.intellij.dbn.execution.statement.variables.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.listener.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.misc.DBNComboBox;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.data.editor.ui.ListPopupValuesProvider;
import com.dci.intellij.dbn.data.editor.ui.TextFieldPopupType;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariable;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesCache;
import com.dci.intellij.dbn.execution.statement.variables.VariableValueProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class StatementExecutionVariableValueForm extends DBNFormBase {
    private JPanel mainPanel;
    private JLabel variableNameLabel;
    private JPanel valueFieldPanel;
    private JLabel errorLabel;
    private JCheckBox useNullCheckBox;
    private JPanel dataTypePanel;

    private final DBNComboBox<GenericDataType> dataTypeComboBox;

    private final StatementExecutionVariable variable;
    private final TextFieldWithPopup<?> editorComponent;

    StatementExecutionVariableValueForm(StatementExecutionInputForm parent, final StatementExecutionVariable variable) {
        super(parent);
        this.variable = variable;
        errorLabel.setVisible(false);
        errorLabel.setIcon(Icons.STMT_EXECUTION_ERROR);

        variableNameLabel.setText(variable.getName().substring(1).trim());
        variableNameLabel.setIcon(Icons.DBO_VARIABLE);

        dataTypeComboBox = new DBNComboBox<>(
                GenericDataType.LITERAL,
                GenericDataType.NUMERIC,
                GenericDataType.DATE_TIME);
        dataTypeComboBox.setSelectedValue(variable.getDataType());
        dataTypePanel.add(dataTypeComboBox, BorderLayout.CENTER);

        StatementExecutionProcessor executionProcessor = parent.getExecutionProcessor();
        Project project = executionProcessor.getProject();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        StatementExecutionVariablesCache variablesCache = executionManager.getVariablesCache();

        editorComponent = new TextFieldWithPopup<>(project);
        editorComponent.createCalendarPopup(false);
        editorComponent.createValuesListPopup(new ListPopupValuesProvider() {
            @Override
            public String getDescription() {
                return "History Values List";
            }

            @Override
            public List<String> getValues() {
                List<String> values = new ArrayList<>();
                VirtualFile virtualFile = executionProcessor.getVirtualFile();
                Set<StatementExecutionVariable> variables = variablesCache.getVariables(virtualFile);
                for (StatementExecutionVariable executionVariable : variables) {
                    if (Objects.equals(executionVariable.getName(), variable.getName())) {
                        Iterable<String> valueHistory = executionVariable.getValueHistory();
                        for (String value : valueHistory) {
                            values.add(value);
                        }
                    }
                }

                return values;
            }

            @Override
            public List<String> getSecondaryValues() {
                return Collections.emptyList();
            }

            @Override
            public boolean isLongLoading() {
                return false;
            }
        }, true);
        editorComponent.setEnabled(!variable.useNull());
        editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, variable.getDataType() == GenericDataType.DATE_TIME);
        valueFieldPanel.add(editorComponent, BorderLayout.CENTER);
        JTextField textField = editorComponent.getTextField();
        String value = variable.getValue();
        if (Strings.isEmpty(value)) {
            VirtualFile virtualFile = executionProcessor.getVirtualFile();
            StatementExecutionVariable cachedVariable = variablesCache.getVariable(virtualFile, variable.getName());
            if (cachedVariable != null) {
                textField.setForeground(UIUtil.getLabelDisabledForeground());
                textField.setText(cachedVariable.getValue());
                textField.getDocument().addDocumentListener(new DocumentAdapter() {
                    @Override
                    protected void textChanged(@NotNull DocumentEvent documentEvent) {
                        textField.setForeground(UIUtil.getTextFieldForeground());
                    }
                });
                dataTypeComboBox.setSelectedValue(cachedVariable.getDataType());
                useNullCheckBox.setSelected(cachedVariable.useNull());
            }
        } else {
            textField.setText(value);
        }


        textField.addKeyListener(ComboBoxSelectionKeyListener.create(dataTypeComboBox, false));

        variable.setPreviewValueProvider(new VariableValueProvider() {
            @Override
            public String getValue() {
                return textField.getText().trim();
            }

            @Override
            public GenericDataType getDataType() {
                return dataTypeComboBox.getSelectedValue();
            }

            @Override
            public boolean useNull() {
                return useNullCheckBox.isSelected();
            }
        });

        dataTypeComboBox.addListener((oldValue, newValue) -> {
            variable.setDataType(newValue);
            editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, newValue == GenericDataType.DATE_TIME);
            getParentForm().updatePreview();
        });

        useNullCheckBox.setSelected(variable.useNull());
        useNullCheckBox.addActionListener(e -> {
            boolean useNullValue = useNullCheckBox.isSelected();
            if (useNullValue) editorComponent.getTextField().setText("");
            editorComponent.setEnabled(!useNullValue);
            editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, dataTypeComboBox.getSelectedValue() == GenericDataType.DATE_TIME);
            getParentForm().updatePreview();
        });

        textField.setToolTipText("<html>While editing variable value, press <b>Up/Down</b> keys to change data type");

        Disposer.register(this, editorComponent);
    }

    public StatementExecutionInputForm getParentForm() {
        return (StatementExecutionInputForm) ensureParent();
    }

    void showErrorLabel(String errorText) {
        errorLabel.setVisible(true);
        errorLabel.setText(errorText);
    }
    
    void hideErrorLabel(){
        errorLabel.setVisible(false);
        errorLabel.setText(null);
    }

    public StatementExecutionVariable getVariable() {
        return variable;
    }

    void saveValue() {
        String trim = editorComponent.getTextField().getText().trim();
        variable.setValue(trim);
        variable.setDataType(dataTypeComboBox.getSelectedValue());
        variable.setUseNull(useNullCheckBox.isSelected());
        StatementExecutionProcessor executionProcessor = getParentForm().getExecutionProcessor();
        Project project = executionProcessor.getProject();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        executionManager.cacheVariable(executionProcessor.getVirtualFile(), variable);
    }

    public void addDocumentListener(DocumentListener documentListener) {
        editorComponent.getTextField().getDocument().addDocumentListener(documentListener);        
    }

    protected int[] getMetrics(int[] metrics) {
        return new int[] {
            (int) Math.max(metrics[0], variableNameLabel.getPreferredSize().getWidth()),
            (int) Math.max(metrics[1], valueFieldPanel.getPreferredSize().getWidth())};
    }

    protected void adjustMetrics(int[] metrics) {
        variableNameLabel.setPreferredSize(new Dimension(metrics[0], variableNameLabel.getHeight()));
        valueFieldPanel.setPreferredSize(new Dimension(metrics[1], valueFieldPanel.getHeight()));
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public JComponent getEditorComponent() {
        return editorComponent.getTextField();
    }

    @Override
    public void disposeInner() {
        variable.setPreviewValueProvider(null);
        super.disposeInner();
    }
}
