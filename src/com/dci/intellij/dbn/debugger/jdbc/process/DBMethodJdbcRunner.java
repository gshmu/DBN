package com.dci.intellij.dbn.debugger.jdbc.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.dci.intellij.dbn.debugger.common.process.DBProgramRunner;
import com.dci.intellij.dbn.debugger.jdbc.config.DBMethodJdbcRunConfig;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.project.Project;

public class DBMethodJdbcRunner extends DBProgramRunner<MethodExecutionInput> {
    public static final String RUNNER_ID = "DBNMethodRunner";

    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (profile instanceof DBMethodJdbcRunConfig) {
            DBMethodJdbcRunConfig runConfiguration = (DBMethodJdbcRunConfig) profile;
            return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && runConfiguration.getMethod() != null;
        }
        return false;
    }

    @Override
    protected DBDebugProcessStarter createProcessStarter(ConnectionHandler connectionHandler) {
        return new DBMethodJdbcProcessStarter(connectionHandler);
    }

    @Override
    protected boolean promptExecutionDialog(MethodExecutionInput executionInput) {
        Project project = executionInput.getProject();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        return executionManager.promptExecutionDialog(executionInput, DBDebuggerType.JDBC);
    }
}

