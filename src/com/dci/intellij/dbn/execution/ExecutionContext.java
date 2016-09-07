package com.dci.intellij.dbn.execution;

import java.sql.Statement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.object.DBSchema;

public abstract class ExecutionContext {
    private boolean isExecuting = false;
    private boolean isExecutionCancelled = false;
    private long executionTimestamp;
    private Statement statement;

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

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public void dismissStatement() {
        ConnectionUtil.cancelStatement(statement);
        ConnectionUtil.closeStatement(statement);
    }
}
