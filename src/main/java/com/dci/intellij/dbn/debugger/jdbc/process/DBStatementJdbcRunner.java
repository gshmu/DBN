package com.dci.intellij.dbn.debugger.jdbc.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.dci.intellij.dbn.debugger.common.process.DBProgramRunner;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DBStatementJdbcRunner extends DBProgramRunner<StatementExecutionInput> {
    public static final String RUNNER_ID = "DBNStatementRunner";

    @Override
    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    protected DBDebugProcessStarter createProcessStarter(ConnectionHandler connection) {
        return new DBStatementJdbcProcessStarter(connection);
    }

    @Override
    protected void promptExecutionDialog(StatementExecutionInput executionInput, Runnable callback) {
        Project project = executionInput.getProject();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        executionManager.promptExecutionDialog(executionInput.getExecutionProcessor(), DBDebuggerType.JDBC, callback);
    }
}

