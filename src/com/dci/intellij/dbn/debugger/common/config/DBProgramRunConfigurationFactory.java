package com.dci.intellij.dbn.debugger.common.config;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;

public abstract class DBProgramRunConfigurationFactory<T extends DBProgramRunConfiguration> extends ConfigurationFactory {
    protected DBProgramRunConfigurationFactory(ConfigurationType type) {
        super(type);
    }
    public abstract T createConfiguration(Project project, String name, boolean generic);
}
