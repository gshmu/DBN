package com.dci.intellij.dbn.debugger.jdwp.config;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.Range;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.options.setting.Settings.*;

@Getter
@Setter
public class DBMethodJdwpRunConfig extends DBMethodRunConfig implements DBJdwpRunConfig {
    private Range<Integer> tcpPortRange = new Range<>(4000, 4999);
    private String tcpHostAddress;

    public DBMethodJdwpRunConfig(Project project, DBMethodJdwpRunConfigFactory factory, String name, DBRunConfigCategory category) {
        super(project, factory, name, category);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DBMethodJdwpRunConfigEditor(this);
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBMethodJdwpRunProfileState(env);
    }

    @Override
    public boolean canRun() {
        return super.canRun() && DBDebuggerType.JDWP.isSupported();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        DatabaseDebuggerManager.checkJdwpConfiguration();
        super.checkConfiguration();
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        Element portsElement = element.getChild("tcp-port-range");
        if (portsElement != null) {
            int fromPortNumber = integerAttribute(portsElement, "from-number", tcpPortRange.getFrom());
            int toPortNumber = integerAttribute(portsElement, "to-number", tcpPortRange.getTo());
            tcpPortRange = new Range<>(fromPortNumber, toPortNumber);
        }

        Element hostElement = element.getChild("tcp-host-address");
        tcpHostAddress = stringAttribute(hostElement, "value");
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        Element portsElement = new Element("tcp-port-range");
        element.addContent(portsElement);
        setIntegerAttribute(portsElement, "from-number", tcpPortRange.getFrom());
        setIntegerAttribute(portsElement, "to-number", tcpPortRange.getTo());

        Element hostElement = new Element("tcp-host-address");
        element.addContent(hostElement);
        setStringAttribute(hostElement, "value", tcpHostAddress);
    }
}
