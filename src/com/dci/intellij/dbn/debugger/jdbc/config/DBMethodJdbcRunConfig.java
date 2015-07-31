package com.dci.intellij.dbn.debugger.jdbc.config;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfig;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;

public class DBMethodJdbcRunConfig extends DBMethodRunConfig {
    public DBMethodJdbcRunConfig(Project project, DBMethodJdbcRunConfigFactory factory, String name, boolean generic) {
        super(project, factory, name, generic);
    }

    @Override
    protected DBMethodJdbcRunConfigEditor createConfigurationEditor() {
        return new DBMethodJdbcRunConfigEditor(this);
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBMethodJdbcRunProfileState(env);
    }
}
