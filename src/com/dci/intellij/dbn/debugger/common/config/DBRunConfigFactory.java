package com.dci.intellij.dbn.debugger.common.config;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;

public abstract class DBRunConfigFactory<T extends DBRunConfigType, C extends DBRunConfig> extends ConfigurationFactory {
    private DBDebuggerType debuggerType;
    protected DBRunConfigFactory(T type, DBDebuggerType debuggerType) {
        super(type);
        this.debuggerType = debuggerType;
    }

    @NotNull
    @Override
    public T getType() {
        return (T) super.getType();
    }

    public abstract C createConfiguration(Project project, String name, boolean generic);

    public DBDebuggerType getDebuggerType(){
        return debuggerType;
    }
}
