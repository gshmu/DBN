package com.dci.intellij.dbn.debugger.common.config;

import com.intellij.execution.configurations.ConfigurationType;

public abstract class DBProgramRunConfigType<T extends DBProgramRunConfigurationFactory> implements ConfigurationType {
    public abstract T[] getConfigurationFactories();
    public T getConfigurationFactory() {
        return getConfigurationFactories()[0];
    }
    public abstract String getDefaultRunnerName();

}
