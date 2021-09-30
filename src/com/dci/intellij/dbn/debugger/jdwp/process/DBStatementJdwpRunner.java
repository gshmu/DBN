package com.dci.intellij.dbn.debugger.jdwp.process;

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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DBStatementJdwpRunner extends DBProgramRunner<StatementExecutionInput> {
    public static final String RUNNER_ID = "DBNStatementJdwpRunner";

    @Override
    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (Objects.equals(executorId, DefaultDebugExecutor.EXECUTOR_ID)) {
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
    protected void promptExecutionDialog(StatementExecutionInput executionInput, Runnable callback) {
        Project project = executionInput.getProject();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        executionManager.promptExecutionDialog(executionInput.getExecutionProcessor(), DBDebuggerType.JDWP, callback);
    }
}

