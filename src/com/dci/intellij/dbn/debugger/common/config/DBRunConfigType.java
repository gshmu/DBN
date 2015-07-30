package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.intellij.execution.configurations.ConfigurationType;

public abstract class DBRunConfigType<T extends DBRunConfigFactory> implements ConfigurationType {
    public abstract T[] getConfigurationFactories();
    public T getConfigurationFactory() {
        return getConfigurationFactories()[0];
    }
    public abstract String getDefaultRunnerName();
    public abstract DBDebuggerType getDebuggerType();

}
