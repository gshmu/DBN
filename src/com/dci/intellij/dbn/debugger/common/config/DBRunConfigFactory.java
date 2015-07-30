package com.dci.intellij.dbn.debugger.common.config;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;

public abstract class DBRunConfigFactory<T extends DBRunConfigType, C extends DBRunConfig> extends ConfigurationFactory {
    protected DBRunConfigFactory(T type) {
        super(type);
    }

    @NotNull
    @Override
    public T getType() {
        return (T) super.getType();
    }

    public abstract C createConfiguration(Project project, String name, boolean generic);
}
