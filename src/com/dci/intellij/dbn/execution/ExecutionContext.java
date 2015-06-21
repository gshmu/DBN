package com.dci.intellij.dbn.execution;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBSchema;

public abstract class ExecutionContext {
    private boolean isExecuting = false;
    private boolean isExecutionCancelled = false;
    private long executionTimestamp;

    public abstract @NotNull String getTargetName();

    public abstract @Nullable ConnectionHandler getTargetConnection();

    public abstract @Nullable DBSchema getTargetSchema();


    public boolean isExecuting() {
        return isExecuting;
    }

    public void setExecuting(boolean isExecuting) {
        this.isExecuting = isExecuting;
    }

    public boolean isExecutionCancelled() {
        return isExecutionCancelled;
    }

    public void setExecutionCancelled(boolean isExecutionCancelled) {
        this.isExecutionCancelled = isExecutionCancelled;
    }

    public long getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(long executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }
}
