package com.dci.intellij.dbn.execution.script.options;

import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.dci.intellij.dbn.execution.script.options.ui.ScriptExecutionSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class ScriptExecutionSettings extends BasicProjectConfiguration<ExecutionEngineSettings, ScriptExecutionSettingsForm> implements ExecutionTimeoutSettings, ProjectSupplier {
    private CmdLineInterfaceBundle commandLineInterfaces = new CmdLineInterfaceBundle();
    private int executionTimeout = 300;

    public ScriptExecutionSettings(ExecutionEngineSettings parent) {
        super(parent);
    }

    @NotNull
    @Override
    public ScriptExecutionSettingsForm createConfigurationEditor() {
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

    @Override
    public int getExecutionTimeout() {
        return executionTimeout;
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
    public boolean setDebugExecutionTimeout(int timeout) {return false;}

    @Override
    public String getConfigElementName() {
        return "script-execution";
    }

    @Override
    public void readConfiguration(Element element) {
        Element executorsElement = element.getChild("command-line-interfaces");
        commandLineInterfaces.readConfiguration(executorsElement);
        executionTimeout = SettingsSupport.getInteger(element, "execution-timeout", executionTimeout);
    }

    @Override
    public void writeConfiguration(Element element) {
        Element executorsElement = new Element("command-line-interfaces");
        commandLineInterfaces.writeConfiguration(executorsElement);
        element.addContent(executorsElement);
        SettingsSupport.setInteger(element, "execution-timeout", executionTimeout);
    }
}
