package com.dci.intellij.dbn.debugger.jdwp.config;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfig;
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
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.integerAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setIntegerAttribute;

public class DBStatementJdwpRunConfig extends DBStatementRunConfig implements DBJdwpRunConfig {
    private Range<Integer> tcpPortRange = new Range<Integer>(4000, 4999);

    public DBStatementJdwpRunConfig(Project project, DBStatementJdwpRunConfigFactory factory, String name, DBRunConfigCategory category) {
        super(project, factory, name, category);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DBStatementJdwpRunConfigEditor(this);
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBStatementJdwpRunProfileState(env);
    }

    @Override
    public boolean canRun() {
        return DBDebuggerType.JDWP.isSupported() && super.canRun();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        DatabaseDebuggerManager.checkJdwpConfiguration();
        super.checkConfiguration();
    }

    @Override
    public Range<Integer> getTcpPortRange() {
        return tcpPortRange;
    }

    public void setTcpPortRange(Range<Integer> tcpPortRange) {
        this.tcpPortRange = tcpPortRange;
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        Element rangeElement = element.getChild("tcp-port-range");
        if (rangeElement != null) {
            int fromPortNumber = integerAttribute(rangeElement, "from-number", tcpPortRange.getFrom());
            int toPortNumber = integerAttribute(rangeElement, "to-number", tcpPortRange.getTo());
            tcpPortRange = new Range<Integer>(fromPortNumber, toPortNumber);
        }
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        Element rangeElement = new Element("tcp-port-range");
        element.addContent(rangeElement);
        setIntegerAttribute(rangeElement, "from-number", tcpPortRange.getFrom());
        setIntegerAttribute(rangeElement, "to-number", tcpPortRange.getTo());
    }
}
