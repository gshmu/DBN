package com.dci.intellij.dbn.execution.script.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.dci.intellij.dbn.execution.script.options.ui.ScriptExecutionSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class ScriptExecutionSettings extends Configuration<ScriptExecutionSettingsForm> implements ExecutionTimeoutSettings{
    private ExecutionEngineSettings parent;
    private CmdLineInterfaceBundle commandLineInterfaces = new CmdLineInterfaceBundle();
    private int executionTimeout = 300;

    public ScriptExecutionSettings(ExecutionEngineSettings parent) {
        this.parent = parent;
    }

    public ExecutionEngineSettings getParent() {
        return parent;
    }

    @NotNull
    @Override
    protected ScriptExecutionSettingsForm createConfigurationEditor() {
        return new ScriptExecutionSettingsForm(this);
    }

    public CmdLineInterfaceBundle getCommandLineInterfaces() {
        return commandLineInterfaces;
    }

    @NotNull
    public CmdLineInterface getCommandLineInterface(String id) {
        return commandLineInterfaces.getInterface(id);
    }

    public void setCommandLineInterfaces(CmdLineInterfaceBundle commandLineInterfaces) {
        this.commandLineInterfaces = commandLineInterfaces;
    }

    @Override
    public int getDebugExecutionTimeout() {
        return 0;
    }

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    public boolean setExecutionTimeout(int executionTimeout) {
        if (this.executionTimeout != executionTimeout) {
            this.executionTimeout = executionTimeout;
            return true;
        }
        return false;
    }

    @Override
    public boolean setDebugExecutionTimeout(int timeout) {return false;}

    @Override
    public String getConfigElementName() {
        return "script-execution";
    }

    @Override
    public void readConfiguration(Element element) {
        Element executorsElement = element.getChild("command-line-interfaces");
        commandLineInterfaces.readConfiguration(executorsElement);
        executionTimeout = SettingsUtil.getInteger(element, "execution-timeout", executionTimeout);
    }

    @Override
    public void writeConfiguration(Element element) {
        Element executorsElement = new Element("command-line-interfaces");
        commandLineInterfaces.writeConfiguration(executorsElement);
        element.addContent(executorsElement);
        SettingsUtil.setInteger(element, "execution-timeout", executionTimeout);
    }
}
