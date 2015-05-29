package com.dci.intellij.dbn.debugger.execution;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.project.Project;

public abstract class DBProgramRunConfiguration extends RunConfigurationBase implements LocatableConfiguration {
    protected DBProgramRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }
}
