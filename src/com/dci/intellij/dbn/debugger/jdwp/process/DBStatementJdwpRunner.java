package com.dci.intellij.dbn.debugger.jdwp.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.dci.intellij.dbn.debugger.common.process.DBProgramRunner;
import com.dci.intellij.dbn.debugger.jdwp.config.DBStatementJdwpRunConfig;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.project.Project;

public class DBStatementJdwpRunner extends DBProgramRunner<StatementExecutionInput> {
    public static final String RUNNER_ID = "DBNStatementJdwpRunner";

    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
            if (profile instanceof DBStatementJdwpRunConfig) {
                DBStatementJdwpRunConfig runConfiguration = (DBStatementJdwpRunConfig) profile;
                return runConfiguration.canRun() && runConfiguration.getExecutionInput() != null;
            }
        }
        return false;
    }

    @Override
    protected DBDebugProcessStarter createProcessStarter(ConnectionHandler connectionHandler) {
        return new DBStatementJdwpProcessStarter(connectionHandler);
    }

    @Override
    protected boolean promptExecutionDialog(StatementExecutionInput executionInput) {
        Project project = executionInput.getProject();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        return executionManager.promptExecutionDialog(executionInput.getExecutionProcessor(), DBDebuggerType.JDWP);
    }
}

