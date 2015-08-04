package com.dci.intellij.dbn.execution;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.intellij.openapi.project.Project;

public interface ExecutionInput extends Disposable, ConnectionProvider {
    Project getProject();

    int getDebugExecutionTimeout();

    @NotNull
    ExecutionContext getExecutionContext();

    int getExecutionTimeout();

    void setExecutionTimeout(int timeout);

    void setDebugExecutionTimeout(int timeout);

    ExecutionTimeoutSettings getExecutionTimeoutSettings();
}
