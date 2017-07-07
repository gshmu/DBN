package com.dci.intellij.dbn.execution.statement.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.TargetConnectionOption;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.common.options.TimeoutSettingsListener;
import com.dci.intellij.dbn.execution.statement.options.ui.StatementExecutionSettingsForm;

public class StatementExecutionSettings extends Configuration implements ExecutionTimeoutSettings {
    public static final String REMEMBER_OPTION_HINT = "\n\n(you can remember your option and change it at any time in Settings > Execution Engine > Statement Execution)";

    private ExecutionEngineSettings parent;
    private int resultSetFetchBlockSize = 100;
    private int executionTimeout = 20;
    private int debugExecutionTimeout = 600;
    private boolean focusResult = false;
    private boolean promptExecution = false;

    private InteractiveOptionHandler<TargetConnectionOption> targetConnection =
            new InteractiveOptionHandler<TargetConnectionOption>(
                    "target-connection",
                    "Target connection",
                    "Please specify the connection to use for executing the statement(s)." +
                            REMEMBER_OPTION_HINT,
                    TargetConnectionOption.ASK,
                    TargetConnectionOption.MAIN,
                    TargetConnectionOption.POOL,
                    TargetConnectionOption.CANCEL);

    public StatementExecutionSettings(ExecutionEngineSettings parent) {
        this.parent = parent;
    }

    public String getDisplayName() {
        return "Statement execution settings";
    }

    public String getHelpTopic() {
        return "executionEngine";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public int getResultSetFetchBlockSize() {
        return resultSetFetchBlockSize;
    }

    public void setResultSetFetchBlockSize(int resultSetFetchBlockSize) {
        this.resultSetFetchBlockSize = resultSetFetchBlockSize;
    }

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    public int getDebugExecutionTimeout() {
        return debugExecutionTimeout;
    }

    public boolean setExecutionTimeout(int executionTimeout) {
        if (this.executionTimeout != executionTimeout) {
            this.executionTimeout = executionTimeout;
            return true;
        }
        return false;
    }

    public boolean setDebugExecutionTimeout(int debugExecutionTimeout) {
        if (this.debugExecutionTimeout != debugExecutionTimeout) {
            this.debugExecutionTimeout = debugExecutionTimeout;
            return true;
        }
        return false;
    }

    void notifyTimeoutChanges() {
        EventUtil.notify(parent.getProject(), TimeoutSettingsListener.TOPIC).settingsChanged(ExecutionTarget.STATEMENT);
    }

    public void setFocusResult(boolean focusResult) {
        this.focusResult = focusResult;
    }

    public boolean isFocusResult() {
        return focusResult;
    }

    public boolean isPromptExecution() {
        return promptExecution;
    }

    public void setPromptExecution(boolean promptExecution) {
        this.promptExecution = promptExecution;
    }

    public InteractiveOptionHandler<TargetConnectionOption> getTargetConnection() {
        return targetConnection;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @NotNull
    public ConfigurationEditorForm createConfigurationEditor() {
        return new StatementExecutionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "statement-execution";
    }

    public void readConfiguration(Element element) {
        resultSetFetchBlockSize = SettingsUtil.getInteger(element, "fetch-block-size", resultSetFetchBlockSize);
        executionTimeout = SettingsUtil.getInteger(element, "execution-timeout", executionTimeout);
        debugExecutionTimeout = SettingsUtil.getInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        focusResult = SettingsUtil.getBoolean(element, "focus-result", focusResult);
        promptExecution = SettingsUtil.getBoolean(element, "prompt-execution", promptExecution);
        targetConnection.readConfiguration(element);
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setInteger(element, "fetch-block-size", resultSetFetchBlockSize);
        SettingsUtil.setInteger(element, "execution-timeout", executionTimeout);
        SettingsUtil.setInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        SettingsUtil.setBoolean(element, "focus-result", focusResult);
        SettingsUtil.setBoolean(element, "prompt-execution", promptExecution);
        targetConnection.writeConfiguration(element);
    }
}
