package com.dci.intellij.dbn.debugger.config;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;

public abstract class DBProgramRunConfigurationFactory extends ConfigurationFactory {
    protected DBProgramRunConfigurationFactory(ConfigurationType type) {
        super(type);
    }
    public abstract DBProgramRunConfiguration createConfiguration(Project project, String name, boolean generic);
}
