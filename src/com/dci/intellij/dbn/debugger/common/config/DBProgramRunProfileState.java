package com.dci.intellij.dbn.debugger.common.config;

import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;

public abstract class DBProgramRunProfileState implements RunProfileState {
    private ExecutionEnvironment environment;

    public DBProgramRunProfileState(ExecutionEnvironment environment) {
        this.environment = environment;
    }

    public ExecutionEnvironment getEnvironment() {
        return environment;
    }
}
