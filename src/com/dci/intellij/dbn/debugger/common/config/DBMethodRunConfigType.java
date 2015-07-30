package com.dci.intellij.dbn.debugger.common.config;

public abstract class DBMethodRunConfigType<T extends DBMethodRunConfigFactory> extends DBRunConfigType<T> {
    @Override
    public T getConfigurationFactory() {
        return super.getConfigurationFactory();
    }
}
