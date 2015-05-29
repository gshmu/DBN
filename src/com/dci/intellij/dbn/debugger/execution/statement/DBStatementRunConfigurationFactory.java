package com.dci.intellij.dbn.debugger.execution.statement;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBStatementRunConfigurationFactory extends DBProgramRunConfigurationFactory {
    protected DBStatementRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXECUTE_SQL_SCRIPT;
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBStatementRunConfiguration(project, this, "");
    }

    public DBStatementRunConfiguration createConfiguration(ConnectionHandler connectionHandler) {
        String name = DatabaseDebuggerManager.createConfigurationName(connectionHandler);
        return new DBStatementRunConfiguration(connectionHandler.getProject(), this, name);
    }

}
