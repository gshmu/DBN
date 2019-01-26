package com.dci.intellij.dbn.execution.method.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.method.options.ui.MethodExecutionSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class MethodExecutionSettings extends Configuration implements ExecutionTimeoutSettings {
    private ExecutionEngineSettings parent;
    private int executionTimeout = 30;
    private int debugExecutionTimeout = 600;
    private int parameterHistorySize = 10;

    public MethodExecutionSettings(ExecutionEngineSettings parent) {
        this.parent = parent;
    }

    @Override
    public String getDisplayName() {
        return "Method execution settings";
    }

    @Override
    public String getHelpTopic() {
        return "executionEngine";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    @Override
    public int getExecutionTimeout() {
        return executionTimeout;
    }

    @Override
    public int getDebugExecutionTimeout() {
        return debugExecutionTimeout;
    }

    @Override
    public boolean setExecutionTimeout(int executionTimeout) {
        if (this.executionTimeout != executionTimeout) {
            this.executionTimeout = executionTimeout;
            return true;
        }
        return false;
    }

    @Override
    public boolean setDebugExecutionTimeout(int debugExecutionTimeout) {
        if (this.debugExecutionTimeout != debugExecutionTimeout) {
            this.debugExecutionTimeout = debugExecutionTimeout;
            return true;
        }
        return false;
    }

    public int getParameterHistorySize() {
        return parameterHistorySize;
    }

    public void setParameterHistorySize(int parameterHistorySize) {
        this.parameterHistorySize = parameterHistorySize;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public ConfigurationEditorForm createConfigurationEditor() {
        return new MethodExecutionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "method-execution";
    }

    @Override
    public void readConfiguration(Element element) {
        executionTimeout = SettingsUtil.getInteger(element, "execution-timeout", executionTimeout);
        debugExecutionTimeout = SettingsUtil.getInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        parameterHistorySize = SettingsUtil.getInteger(element, "parameter-history-size", parameterHistorySize);

    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setInteger(element, "execution-timeout", executionTimeout);
        SettingsUtil.setInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        SettingsUtil.setInteger(element, "parameter-history-size", parameterHistorySize);
    }
}
