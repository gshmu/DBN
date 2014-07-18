package com.dci.intellij.dbn.execution.statement.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.statement.options.ui.StatementExecutionSettingsForm;
import org.jdom.Element;

public class StatementExecutionSettings extends Configuration{
    private int resultSetFetchBlockSize = 100;
    private int executionTimeout = 20;
    private boolean focusResult = false;

    public String getDisplayName() {
        return "Data editor general settings";
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

    public void setFocusResult(boolean focusResult) {
        this.focusResult = focusResult;
    }

    public boolean isFocusResult() {
        return focusResult;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
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
        focusResult = SettingsUtil.getBoolean(element, "focus-result", focusResult);

    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setInteger(element, "fetch-block-size", resultSetFetchBlockSize);
        SettingsUtil.setInteger(element, "execution-timeout", executionTimeout);
        SettingsUtil.setBoolean(element, "focus-result", focusResult);
    }
}
