package com.dci.intellij.dbn.execution.method.options.ui;

import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.common.options.TimeoutSettingsListener;
import com.dci.intellij.dbn.execution.method.options.MethodExecutionSettings;
import com.intellij.openapi.options.ConfigurationException;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class MethodExecutionSettingsForm extends ConfigurationEditorForm<MethodExecutionSettings> {
    private JPanel mainPanel;
    private JTextField executionTimeoutTextField;
    private JTextField debugExecutionTimeoutTextField;
    private JTextField parameterHistorySizeTextField;

    public MethodExecutionSettingsForm(MethodExecutionSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        MethodExecutionSettings settings = getConfiguration();
        int executionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(executionTimeoutTextField, "Execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");
        int debugExecutionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(debugExecutionTimeoutTextField, "Debug execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");
        int parameterHistorySize = ConfigurationEditorUtil.validateIntegerInputValue(parameterHistorySizeTextField, "Parameter history size", true, 0, 3000, null);
        settings.setParameterHistorySize(parameterHistorySize);

        boolean timeoutSettingsChanged = settings.setExecutionTimeout(executionTimeout);
        timeoutSettingsChanged = settings.setDebugExecutionTimeout(debugExecutionTimeout) || timeoutSettingsChanged;
        if (timeoutSettingsChanged) {
            new SettingsChangeNotifier() {
                @Override
                public void notifyChanges() {
                    EventUtil.notify(getProject(), TimeoutSettingsListener.TOPIC).settingsChanged(ExecutionTarget.METHOD);
                }
            };
        }
    }

    public void resetFormChanges() {
        MethodExecutionSettings settings = getConfiguration();
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        debugExecutionTimeoutTextField.setText(Integer.toString(settings.getDebugExecutionTimeout()));
        parameterHistorySizeTextField.setText(Integer.toString(settings.getParameterHistorySize()));
    }
}
