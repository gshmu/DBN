package com.dci.intellij.dbn.execution.statement.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.statement.options.ui.StatementExecutionSettingsForm;

public class StatementExecutionSettings extends Configuration{
    private int resultSetFetchBlockSize = 100;
    private int executionTimeout = 20;
    private int debugExecutionTimeout = 600;
    private boolean focusResult = false;

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

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    public int getDebugExecutionTimeout() {
        return debugExecutionTimeout;
    }

    public void setDebugExecutionTimeout(int debugExecutionTimeout) {
        this.debugExecutionTimeout = debugExecutionTimeout;
    }

    public void setFocusResult(boolean focusResult) {
        this.focusResult = focusResult;
    }

    public boolean isFocusResult() {
        return focusResult;
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

    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setInteger(element, "fetch-block-size", resultSetFetchBlockSize);
        SettingsUtil.setInteger(element, "execution-timeout", executionTimeout);
        SettingsUtil.setInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        SettingsUtil.setBoolean(element, "focus-result", focusResult);
    }
}
