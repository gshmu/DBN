package com.dci.intellij.dbn.execution;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.intellij.openapi.project.Project;

public interface ExecutionInput extends Disposable, ConnectionProvider {
    Project getProject();

    @NotNull
    ExecutionContext getExecutionContext();
}
