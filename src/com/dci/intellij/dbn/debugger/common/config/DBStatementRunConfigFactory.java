package com.dci.intellij.dbn.debugger.common.config;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.intellij.openapi.project.Project;

public abstract class DBStatementRunConfigFactory<T extends DBStatementRunConfigType, C extends DBStatementRunConfig> extends DBRunConfigFactory<T, C> {
    protected DBStatementRunConfigFactory(T type, DBDebuggerType debuggerType) {
        super(type, debuggerType);
    }

    @NotNull
    @Override
    public T getType() {
        return (T) super.getType();
    }

    public abstract C createConfiguration(Project project, String name, DBRunConfigCategory category);
}
