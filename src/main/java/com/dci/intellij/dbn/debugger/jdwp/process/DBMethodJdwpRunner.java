package com.dci.intellij.dbn.debugger.jdwp.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.dci.intellij.dbn.debugger.common.process.DBProgramRunner;
import com.dci.intellij.dbn.debugger.jdwp.config.DBMethodJdwpRunConfig;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DBMethodJdwpRunner extends DBProgramRunner<MethodExecutionInput> {
    public static final String RUNNER_ID = "DBNMethodJdwpRunner";

    @Override
    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (Objects.equals(executorId, DefaultDebugExecutor.EXECUTOR_ID)) {
            if (profile instanceof DBMethodJdwpRunConfig) {
                DBMethodJdwpRunConfig runConfiguration = (DBMethodJdwpRunConfig) profile;
                return runConfiguration.canRun() && runConfiguration.getMethod() != null;
            }
        }
        return false;
    }

    @Override
    protected DBDebugProcessStarter createProcessStarter(ConnectionHandler connection) {
        return new DBMethodJdwpProcessStarter(connection);
    }

    @Override
    protected void promptExecutionDialog(MethodExecutionInput executionInput, Runnable callback) {
        Project project = executionInput.getProject();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        executionManager.promptExecutionDialog(executionInput, DBDebuggerType.JDWP, callback);
    }
}

