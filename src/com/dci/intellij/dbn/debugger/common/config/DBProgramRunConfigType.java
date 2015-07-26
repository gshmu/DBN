package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.intellij.execution.configurations.ConfigurationType;

public abstract class DBProgramRunConfigType<T extends DBProgramRunConfigurationFactory> implements ConfigurationType {
    public abstract T[] getConfigurationFactories();
    public T getConfigurationFactory() {
        return getConfigurationFactories()[0];
    }
    public abstract String getDefaultRunnerName();
    public abstract DBDebuggerType getDebuggerType();

}
