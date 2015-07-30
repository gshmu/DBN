package com.dci.intellij.dbn.debugger.common.config.ui;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.intellij.openapi.options.ConfigurationException;

public abstract class DBProgramRunConfigurationEditorForm<T extends DBRunConfig> extends DBNFormImpl {
    private T configuration;

    public DBProgramRunConfigurationEditorForm(T configuration) {
        super(configuration.getProject());
        this.configuration = configuration;
    }

    @NotNull
    public T getConfiguration() {
        return FailsafeUtil.get(configuration);
    }

    @Override
    public void dispose() {
        configuration = null;
    }

    public abstract void readConfiguration(T configuration);

    public abstract void writeConfiguration(T configuration) throws ConfigurationException;
}
