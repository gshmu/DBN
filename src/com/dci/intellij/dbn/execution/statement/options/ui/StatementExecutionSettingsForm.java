package com.dci.intellij.dbn.execution.statement.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.TargetConnectionOption;
import com.dci.intellij.dbn.execution.common.options.TimeoutSettingsListener;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.intellij.openapi.options.ConfigurationException;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class StatementExecutionSettingsForm extends ConfigurationEditorForm<StatementExecutionSettings> {
    private JPanel mainPanel;
    private JTextField fetchBlockSizeTextField;
    private JTextField executionTimeoutTextField;
    private JCheckBox focusResultCheckBox;
    private JTextField debugExecutionTimeoutTextField;
    private JCheckBox promptExecutionCheckBox;
    private DBNComboBox<TargetConnectionOption> targetConnectionComboBox;

    public StatementExecutionSettingsForm(StatementExecutionSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        targetConnectionComboBox.setValues(
                TargetConnectionOption.ASK,
                TargetConnectionOption.MAIN,
                TargetConnectionOption.POOL);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        StatementExecutionSettings settings = getConfiguration();
        settings.setResultSetFetchBlockSize(ConfigurationEditorUtil.validateIntegerInputValue(fetchBlockSizeTextField, "Fetch block size", true, 1, 10000, null));
        int executionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(executionTimeoutTextField, "Execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");
        int debugExecutionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(debugExecutionTimeoutTextField, "Debug execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");

        settings.setFocusResult(focusResultCheckBox.isSelected());
        settings.setPromptExecution(promptExecutionCheckBox.isSelected());

        settings.getTargetConnection().set(targetConnectionComboBox.getSelectedValue());

        boolean timeoutSettingsChanged = settings.setExecutionTimeout(executionTimeout);
        timeoutSettingsChanged = settings.setDebugExecutionTimeout(debugExecutionTimeout) || timeoutSettingsChanged;
        if (timeoutSettingsChanged) {
            new SettingsChangeNotifier() {
                @Override
                public void notifyChanges() {
                    EventUtil.notify(getProject(), TimeoutSettingsListener.TOPIC).settingsChanged(ExecutionTarget.STATEMENT);
                }
            };
        }
    }

    public void resetFormChanges() {
        StatementExecutionSettings settings = getConfiguration();
        fetchBlockSizeTextField.setText(Integer.toString(settings.getResultSetFetchBlockSize()));
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        debugExecutionTimeoutTextField.setText(Integer.toString(settings.getDebugExecutionTimeout()));
        focusResultCheckBox.setSelected(settings.isFocusResult());
        promptExecutionCheckBox.setSelected(settings.isPromptExecution());
        targetConnectionComboBox.setSelectedValue(settings.getTargetConnection().get());
    }
}
