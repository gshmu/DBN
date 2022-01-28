package com.dci.intellij.dbn.execution.method.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.execution.method.options.MethodExecutionSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class MethodExecutionSettingsForm extends ConfigurationEditorForm<MethodExecutionSettings> {
    private JPanel mainPanel;
    private JTextField executionTimeoutTextField;
    private JTextField debugExecutionTimeoutTextField;
    private JTextField parameterHistorySizeTextField;

    public MethodExecutionSettingsForm(MethodExecutionSettings settings) {
        super(settings);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        MethodExecutionSettings configuration = getConfiguration();
        int executionTimeout = ConfigurationEditorUtil.validateIntegerValue(executionTimeoutTextField, "Execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");
        int debugExecutionTimeout = ConfigurationEditorUtil.validateIntegerValue(debugExecutionTimeoutTextField, "Debug execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");
        int parameterHistorySize = ConfigurationEditorUtil.validateIntegerValue(parameterHistorySizeTextField, "Parameter history size", true, 0, 3000, null);
        configuration.setParameterHistorySize(parameterHistorySize);

        configuration.setExecutionTimeout(executionTimeout);
        configuration.setDebugExecutionTimeout(debugExecutionTimeout);
    }

    @Override
    public void resetFormChanges() {
        MethodExecutionSettings settings = getConfiguration();
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        debugExecutionTimeoutTextField.setText(Integer.toString(settings.getDebugExecutionTimeout()));
        parameterHistorySizeTextField.setText(Integer.toString(settings.getParameterHistorySize()));
    }
}
