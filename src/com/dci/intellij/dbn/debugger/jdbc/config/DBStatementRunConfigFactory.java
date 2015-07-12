package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class DBStatementRunConfigFactory extends DBProgramRunConfigurationFactory {
    protected DBStatementRunConfigFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public DBProgramRunConfiguration createConfiguration(Project project, String name, boolean generic) {
        return new DBStatementRunConfig(project, this, name, generic);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBStatementRunConfig(project, this, "", false);
    }
}
