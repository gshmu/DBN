package com.dci.intellij.dbn.debugger.jdwp.config;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigFactory;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigType;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBMethodJdwpRunConfigFactory extends DBMethodRunConfigFactory<DBMethodRunConfigType, DBMethodJdwpRunConfig> {
    public DBMethodJdwpRunConfigFactory(@NotNull DBMethodRunConfigType type) {
        super(type, DBDebuggerType.JDWP);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBMethodJdwpRunConfig(project, this, "", DBRunConfigCategory.TEMPLATE);
    }

    @Override
    public DBMethodJdwpRunConfig createConfiguration(Project project, String name, DBRunConfigCategory category) {
        return new DBMethodJdwpRunConfig(project, this, name, category);
    }

    public DBMethodJdwpRunConfig createConfiguration(DBMethod method) {
        String name = DatabaseDebuggerManager.createMethodConfigurationName(method);
        name = name + " (JDWP)";
        DBMethodJdwpRunConfig runConfiguration = new DBMethodJdwpRunConfig(method.getProject(), this, name, DBRunConfigCategory.CUSTOM);
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(method.getProject());
        MethodExecutionInput executionInput = executionManager.getExecutionInput(method);
        runConfiguration.setExecutionInput(executionInput);
        return runConfiguration;
    }

    @Override
    public String getName() {
        return super.getName() + " (JDWP)";
    }

}
