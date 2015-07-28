package com.dci.intellij.dbn.debugger.jdwp.config;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigType;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.Range;

public class DBMethodJdwpRunConfig extends DBMethodRunConfig {
    private Range<Integer> tcpPortRange = new Range<Integer>(4000, 4999);

    public DBMethodJdwpRunConfig(Project project, DBMethodRunConfigType configType, String name, boolean generic) {
        super(project, configType, name, generic);
    }

    @Override
    protected DBMethodJdwpRunConfigEditor createConfigurationEditor() {
        return new DBMethodJdwpRunConfigEditor(this);
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBMethodJdwpRunProfileState(env);
    }

    public Range<Integer> getTcpPortRange() {
        return tcpPortRange;
    }

    public void setTcpPortRange(Range<Integer> tcpPortRange) {
        this.tcpPortRange = tcpPortRange;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        Element rangeElement = element.getChild("tcp-port-range");
        if (rangeElement != null) {
            int fromPortNumber = SettingsUtil.getIntegerAttribute(rangeElement, "from-number", tcpPortRange.getFrom());
            int toPortNumber = SettingsUtil.getIntegerAttribute(rangeElement, "to-number", tcpPortRange.getTo());
            tcpPortRange = new Range<Integer>(fromPortNumber, toPortNumber);
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        Element rangeElement = new Element("tcp-port-range");
        element.addContent(rangeElement);
        SettingsUtil.setIntegerAttribute(rangeElement, "from-number", tcpPortRange.getFrom());
        SettingsUtil.setIntegerAttribute(rangeElement, "to-number", tcpPortRange.getTo());
    }
}
