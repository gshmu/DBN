package com.dci.intellij.dbn.debugger.common.config;

public abstract class DBStatementRunConfigType<T extends DBStatementRunConfigFactory> extends DBRunConfigType<T> {
    @Override
    public T getConfigurationFactory() {
        return super.getConfigurationFactory();
    }
}
