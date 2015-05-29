package com.dci.intellij.dbn.debugger.execution.method;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.execution.DBProgramRunProfileState;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.runners.ProgramRunner;


public class DBMethodRunProfileState extends DBProgramRunProfileState {
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        return null;
    }

    public RunnerSettings getRunnerSettings() {
        return null;
    }

    public ConfigurationPerRunnerSettings getConfigurationSettings() {
        return null;
    }
}
