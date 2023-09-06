package com.dci.intellij.dbn.debugger.jdbc.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.dci.intellij.dbn.debugger.common.process.DBProgramRunner;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DBMethodJdbcRunner extends DBProgramRunner<MethodExecutionInput> {
    public static final String RUNNER_ID = "DBNMethodRunner";

    @Override
    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    protected DBDebugProcessStarter createProcessStarter(ConnectionHandler connection) {
        return new DBMethodJdbcProcessStarter(connection);
    }

    @Override
    protected void promptExecutionDialog(MethodExecutionInput executionInput, Runnable callback) {
        Project project = executionInput.getProject();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        executionManager.promptExecutionDialog(executionInput, DBDebuggerType.JDBC, callback);
    }
}

