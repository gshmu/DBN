package com.dci.intellij.dbn.debugger.common.config;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;

public abstract class DBStatementRunConfigFactory<T extends DBStatementRunConfigType, C extends DBStatementRunConfig> extends DBProgramRunConfigFactory<T, C> {
    protected DBStatementRunConfigFactory(T type) {
        super(type);
    }

    @NotNull
    @Override
    public T getType() {
        return (T) super.getType();
    }

    public abstract C createConfiguration(Project project, String name, boolean generic);
}
