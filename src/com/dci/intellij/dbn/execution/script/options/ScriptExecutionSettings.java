package com.dci.intellij.dbn.execution.script.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.dci.intellij.dbn.execution.script.options.ui.ScriptExecutionSettingsForm;

public class ScriptExecutionSettings extends Configuration<ScriptExecutionSettingsForm> {
    private ExecutionEngineSettings parent;
    private CmdLineInterfaceBundle commandLineInterfaces = new CmdLineInterfaceBundle();

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
    public String getConfigElementName() {
        return "script-execution";
    }

    @Override
    public void readConfiguration(Element element) {
        Element executorsElement = element.getChild("command-line-interfaces");
        commandLineInterfaces.readConfiguration(executorsElement);
    }

    @Override
    public void writeConfiguration(Element element) {
        Element executorsElement = new Element("command-line-interfaces");
        commandLineInterfaces.writeConfiguration(executorsElement);
        element.addContent(executorsElement);
    }
}
