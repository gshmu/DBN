package com.dci.intellij.dbn.debugger.common.config;

public abstract class DBStatementRunConfigType<T extends DBStatementRunConfigFactory> extends DBProgramRunConfigType<T> {
    @Override
    public T getConfigurationFactory() {
        return super.getConfigurationFactory();
    }
}
