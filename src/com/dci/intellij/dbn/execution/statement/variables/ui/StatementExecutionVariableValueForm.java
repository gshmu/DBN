package com.dci.intellij.dbn.execution.statement.variables.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.StringUtil;
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


public class StatementExecutionVariableValueForm extends DBNFormImpl<StatementExecutionVariablesForm> {
    private JPanel mainPanel;
    private JLabel variableNameLabel;
    private JPanel valueFieldPanel;
    private JLabel errorLabel;
    private JCheckBox useNullCheckBox;
    private JPanel dataTypePanel;

    private DBNComboBox<GenericDataType> dataTypeComboBox;

    private StatementExecutionVariable variable;
    private TextFieldWithPopup editorComponent;

    public StatementExecutionVariableValueForm(StatementExecutionVariablesForm parentComponent, final StatementExecutionVariable variable) {
        super(parentComponent);
        this.variable = variable;
        errorLabel.setVisible(false);
        errorLabel.setIcon(Icons.STMT_EXECUTION_ERROR);

        variableNameLabel.setText(variable.getName().substring(1).trim());
        variableNameLabel.setIcon(Icons.DBO_VARIABLE);

        dataTypeComboBox = new DBNComboBox<GenericDataType>(
                Arrays.asList(
                    GenericDataType.LITERAL,
                    GenericDataType.NUMERIC,
                    GenericDataType.DATE_TIME));
        dataTypeComboBox.setSelectedValue(variable.getDataType());
        dataTypePanel.add(dataTypeComboBox, BorderLayout.CENTER);

        StatementExecutionProcessor executionProcessor = parentComponent.getExecutionProcessor();
        Project project = executionProcessor.getProject();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        final StatementExecutionVariablesCache variablesCache = executionManager.getVariablesCache();
        final VirtualFile virtualFile = executionProcessor.getVirtualFile();

        editorComponent = new TextFieldWithPopup(project);
        editorComponent.createCalendarPopup(false);
        editorComponent.createValuesListPopup(new ListPopupValuesProvider() {
            @Override
            public String getDescription() {
                return "History Values List";
            }

            @Override
            public List<String> getValues() {
                List<String> values = new ArrayList<String>();
                final Set<StatementExecutionVariable> variables = variablesCache.getVariables(virtualFile);
                for (StatementExecutionVariable executionVariable : variables) {
                    if (executionVariable.getName().equals(variable.getName())) {
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
        final JTextField textField = editorComponent.getTextField();
        String value = variable.getValue();
        if (StringUtil.isEmpty(value)) {
            StatementExecutionVariable cachedVariable = variablesCache.getVariable(virtualFile, variable.getName());
            if (cachedVariable != null) {
                textField.setForeground(UIUtil.getLabelDisabledForeground());
                textField.setText(cachedVariable.getValue());
                textField.getDocument().addDocumentListener(new DocumentAdapter() {
                    @Override
                    protected void textChanged(DocumentEvent documentEvent) {
                    textField.setForeground(UIUtil.getTextFieldForeground());
                    }
                });
            }
        } else {
            textField.setText(value);
        }


        textField.addKeyListener(ComboBoxSelectionKeyListener.create(dataTypeComboBox, false));

        variable.setPreviewValueProvider(new VariableValueProvider() {
            public String getValue() {
                return textField.getText().trim();
            }

            public GenericDataType getDataType() {
                return dataTypeComboBox.getSelectedValue();
            }

            @Override
            public boolean useNull() {
                return useNullCheckBox.isSelected();
            }
        });

        dataTypeComboBox.addListener(new ValueSelectorListener<GenericDataType>() {
            @Override
            public void selectionChanged(GenericDataType oldValue, GenericDataType newValue) {
                editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, oldValue == GenericDataType.DATE_TIME);
                getParentComponent().updatePreview();
            }
        });

        useNullCheckBox.setSelected(variable.useNull());
        useNullCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean useNullValue = useNullCheckBox.isSelected();
                if (useNullValue) editorComponent.getTextField().setText("");
                editorComponent.setEnabled(!useNullValue);
                editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, dataTypeComboBox.getSelectedValue() == GenericDataType.DATE_TIME);
                getParentComponent().updatePreview();
            }
        });

        textField.setToolTipText("<html>While editing variable value, press <b>Up/Down</b> keys to change data type");

        Disposer.register(this, editorComponent);
    }

    public void showErrorLabel(String errorText) {
        errorLabel.setVisible(true);
        errorLabel.setText(errorText);
    }
    
    public void hideErrorLabel(){
        errorLabel.setVisible(false);
        errorLabel.setText(null);
    }

    public StatementExecutionVariable getVariable() {
        return variable;
    }

    public void saveValue() {
        String trim = editorComponent.getTextField().getText().trim();
        variable.setValue(trim);
        variable.setDataType(dataTypeComboBox.getSelectedValue());
        variable.setUseNull(useNullCheckBox.isSelected());
        StatementExecutionProcessor executionProcessor = getParentComponent().getExecutionProcessor();
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


    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        variable.setPreviewValueProvider(null);
        variable = null;
        editorComponent = null;
    }

    public JComponent getEditorComponent() {
        return editorComponent.getTextField();
    }
}
