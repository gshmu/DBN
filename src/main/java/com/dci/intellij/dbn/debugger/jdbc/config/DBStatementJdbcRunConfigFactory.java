package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigFactory;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DBStatementJdbcRunConfigFactory extends DBStatementRunConfigFactory<DBStatementRunConfigType, DBStatementJdbcRunConfig> {
    public DBStatementJdbcRunConfigFactory(@NotNull DBStatementRunConfigType type) {
        super(type, DBDebuggerType.JDBC);
    }

    @Override
    public DBStatementJdbcRunConfig createConfiguration(Project project, String name, DBRunConfigCategory category) {
        return new DBStatementJdbcRunConfig(project, this, name, category);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new DBStatementJdbcRunConfig(project, this, "", DBRunConfigCategory.TEMPLATE);
    }
}
