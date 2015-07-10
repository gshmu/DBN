package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.config.DBProgramRunConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;

public abstract class DBProgramRunConfigurationType implements ConfigurationType {
    public abstract DBProgramRunConfigurationFactory getConfigurationFactory();
    public abstract String getDefaultRunnerName();
}
