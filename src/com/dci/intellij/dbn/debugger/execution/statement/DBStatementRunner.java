package com.dci.intellij.dbn.debugger.execution.statement;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.execution.DBProgramRunner;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.intellij.execution.configurations.RunProfile;

public class DBStatementRunner extends DBProgramRunner<StatementExecutionInput> {
    public static final String RUNNER_ID = "DBNStatementRunner";

    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return false;
    }

    @Override
    protected boolean promptExecutionDialog(StatementExecutionInput executionInput) {
        return true;
    }
}

