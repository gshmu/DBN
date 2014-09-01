package com.dci.intellij.dbn.execution.method.options.ui;

import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.execution.method.options.MethodExecutionSettings;
import com.intellij.openapi.options.ConfigurationException;

public class MethodExecutionSettingsForm extends ConfigurationEditorForm<MethodExecutionSettings> {
    private JPanel mainPanel;
    private JTextField executionTimeoutTextField;
    private JTextField debugExecutionTimeoutTextField;

    public MethodExecutionSettingsForm(MethodExecutionSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        resetChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        MethodExecutionSettings settings = getConfiguration();
        int executionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(executionTimeoutTextField, "Execution timeout", 0, 300, "\nUse value 0 for no timeout");
        int debugExecutionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(debugExecutionTimeoutTextField, "Debug execution timeout", 0, 3000, "\nUse value 0 for no timeout");
        settings.setExecutionTimeout(executionTimeout);
        settings.setDebugExecutionTimeout(debugExecutionTimeout);
    }

    public void resetChanges() {
        MethodExecutionSettings settings = getConfiguration();
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        debugExecutionTimeoutTextField.setText(Integer.toString(settings.getDebugExecutionTimeout()));
    }
}
