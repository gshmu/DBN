package com.dci.intellij.dbn.debugger.jdwp.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.jdbc.process.DBProgramDebugProcessStarter;
import com.dci.intellij.dbn.debugger.jdbc.process.DBProgramRunner;
import com.dci.intellij.dbn.debugger.jdwp.config.DBMethodJdwpRunConfig;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.project.Project;

public class DBMethodJdwpRunner extends DBProgramRunner<MethodExecutionInput> {
    public static final String RUNNER_ID = "DBNMethodJdwpRunner";

    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
            if (profile instanceof DBMethodJdwpRunConfig) {
                DBMethodJdwpRunConfig runConfiguration = (DBMethodJdwpRunConfig) profile;
                return runConfiguration.getMethod() != null;
            }
        }
        return false;
    }

    @Override
    protected DBProgramDebugProcessStarter createProcessStarter(ConnectionHandler connectionHandler) {
        return new DBMethodJdwpProcessStarter(connectionHandler);
    }

    @Override
    protected boolean promptExecutionDialog(MethodExecutionInput executionInput) {
        Project project = executionInput.getProject();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        return executionManager.promptExecutionDialog(executionInput, true);
    }
}

