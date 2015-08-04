package com.dci.intellij.dbn.execution.statement.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.intellij.openapi.options.ConfigurationException;

public class StatementExecutionSettingsForm extends ConfigurationEditorForm<StatementExecutionSettings> {
    private JPanel mainPanel;
    private JTextField fetchBlockSizeTextField;
    private JTextField executionTimeoutTextField;
    private JCheckBox focusResultCheckBox;
    private JTextField debugExecutionTimeoutTextField;
    private JCheckBox promptExecutionCheckBox;

    public StatementExecutionSettingsForm(StatementExecutionSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        StatementExecutionSettings settings = getConfiguration();
        settings.setResultSetFetchBlockSize(ConfigurationEditorUtil.validateIntegerInputValue(fetchBlockSizeTextField, "Fetch block size", true, 1, 10000, null));
        settings.setExecutionTimeout(ConfigurationEditorUtil.validateIntegerInputValue(executionTimeoutTextField, "Execution timeout", true, 0, 6000, "\nUse value 0 for no timeout"));
        settings.setDebugExecutionTimeout(ConfigurationEditorUtil.validateIntegerInputValue(debugExecutionTimeoutTextField, "Debug execution timeout", true, 0, 6000, "\nUse value 0 for no timeout"));
        settings.setFocusResult(focusResultCheckBox.isSelected());
        settings.setPromptExecution(promptExecutionCheckBox.isSelected());
    }

    public void resetFormChanges() {
        StatementExecutionSettings settings = getConfiguration();
        fetchBlockSizeTextField.setText(Integer.toString(settings.getResultSetFetchBlockSize()));
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        debugExecutionTimeoutTextField.setText(Integer.toString(settings.getDebugExecutionTimeout()));
        focusResultCheckBox.setSelected(settings.isFocusResult());
        promptExecutionCheckBox.setSelected(settings.isPromptExecution());
    }
}
