package com.dci.intellij.dbn.debugger.jdbc.config;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigFactory;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBMethodJdbcRunConfigFactory extends DBMethodRunConfigFactory<DBMethodJdbcRunConfig> {
    protected DBMethodJdbcRunConfigFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBMethodJdbcRunConfig(project, this, "", false);
    }

    @Override
    public DBMethodJdbcRunConfig createConfiguration(Project project, String name, boolean generic) {
        return new DBMethodJdbcRunConfig(project, this, name, generic);
    }

    @Override
    public DBMethodJdbcRunConfig createConfiguration(DBMethod method) {
        String name = DatabaseDebuggerManager.createMethodConfigurationName(method, DBDebuggerType.JDBC);
        DBMethodJdbcRunConfig runConfiguration = new DBMethodJdbcRunConfig(method.getProject(), this, name, false);
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(method.getProject());
        MethodExecutionInput executionInput = executionManager.getExecutionInput(method);
        runConfiguration.setExecutionInput(executionInput);
        return runConfiguration;
    }

}
