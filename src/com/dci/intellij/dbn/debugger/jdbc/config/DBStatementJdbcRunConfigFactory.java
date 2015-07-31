package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigFactory;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBStatementJdbcRunConfigFactory extends DBStatementRunConfigFactory<DBStatementRunConfigType, DBStatementJdbcRunConfig> {
    public DBStatementJdbcRunConfigFactory(@NotNull DBStatementRunConfigType type) {
        super(type, DBDebuggerType.JDBC);
    }

    @Override
    public DBStatementJdbcRunConfig createConfiguration(Project project, String name, boolean generic) {
        return new DBStatementJdbcRunConfig(project, this, name, generic);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBStatementJdbcRunConfig(project, this, "", false);
    }
}
