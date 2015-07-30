package com.dci.intellij.dbn.debugger.jdwp.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBStatementJdwpRunConfigFactory extends DBStatementRunConfigFactory<DBStatementJdwpRunConfigType, DBStatementJdwpRunConfig> {
    protected DBStatementJdwpRunConfigFactory(@NotNull DBStatementJdwpRunConfigType type) {
        super(type);
    }

    @Override
    public DBStatementJdwpRunConfig createConfiguration(Project project, String name, boolean generic) {
        return new DBStatementJdwpRunConfig(project, getType(), name, generic);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBStatementJdwpRunConfig(project, getType(), "", false);
    }
}
