package com.dci.intellij.dbn.debugger.execution.method;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.execution.DBProgramRunner;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.project.Project;

public class DBMethodRunner extends DBProgramRunner<MethodExecutionInput> {
    public static final String RUNNER_ID = "DBNMethodRunner";

    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (profile instanceof DBMethodRunConfiguration) {
            DBMethodRunConfiguration runConfiguration = (DBMethodRunConfiguration) profile;
            return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && runConfiguration.getMethod() != null;
        }
        return false;
    }

    @Override
    protected boolean promptExecutionDialog(MethodExecutionInput executionInput) {
        Project project = executionInput.getProject();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        return executionManager.promptExecutionDialog(executionInput, true);
    }
}

