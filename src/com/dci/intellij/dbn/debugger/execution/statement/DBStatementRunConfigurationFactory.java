package com.dci.intellij.dbn.debugger.execution.statement;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class DBStatementRunConfigurationFactory extends DBProgramRunConfigurationFactory {
    protected DBStatementRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public DBProgramRunConfiguration createConfiguration(Project project, String name, boolean generic) {
        return new DBStatementRunConfiguration(project, this, name, generic);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBStatementRunConfiguration(project, this, "", false);
    }
}
