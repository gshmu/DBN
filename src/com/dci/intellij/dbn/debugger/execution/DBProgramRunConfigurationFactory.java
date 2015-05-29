package com.dci.intellij.dbn.debugger.execution;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;

public abstract class DBProgramRunConfigurationFactory extends ConfigurationFactory {
    protected DBProgramRunConfigurationFactory(ConfigurationType type) {
        super(type);
    }
}
