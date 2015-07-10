package com.dci.intellij.dbn.debugger.jdbc.config.ui;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.debugger.config.DBProgramRunConfiguration;

public abstract class DBProgramRunConfigurationEditorForm<T extends DBProgramRunConfiguration> extends DBNFormImpl {
    private T configuration;

    public DBProgramRunConfigurationEditorForm(T configuration) {
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

    public abstract void writeConfiguration(T configuration);
}
