package com.dci.intellij.dbn.debugger.execution;

import com.intellij.execution.configurations.ConfigurationType;

public abstract class DBProgramRunConfigurationType implements ConfigurationType {
    public abstract DBProgramRunConfigurationFactory getConfigurationFactory();
    public abstract String getDefaultRunnerName();
}
