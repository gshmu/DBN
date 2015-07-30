package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBStatementJdbcRunConfigFactory extends DBStatementRunConfigFactory<DBStatementJdbcRunConfigType, DBStatementJdbcRunConfig> {
    protected DBStatementJdbcRunConfigFactory(@NotNull DBStatementJdbcRunConfigType type) {
        super(type);
    }

    @Override
    public DBStatementJdbcRunConfig createConfiguration(Project project, String name, boolean generic) {
        return new DBStatementJdbcRunConfig(project, getType(), name, generic);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBStatementJdbcRunConfig(project, getType(), "", false);
    }
}
