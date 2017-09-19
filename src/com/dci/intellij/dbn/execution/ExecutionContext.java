package com.dci.intellij.dbn.execution;

import java.sql.Statement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.object.DBSchema;

public abstract class ExecutionContext {
    private transient int timeout;
    private transient boolean busy = false;
    private transient boolean logging = false;
    private transient boolean executing = false;
    private transient boolean cancelled = false;
    private transient long executionTimestamp;
    private transient DBNConnection connection;
    private transient Statement statement;

    public abstract @NotNull String getTargetName();

    public abstract @Nullable ConnectionHandler getTargetConnection();

    public abstract @Nullable DBSchema getTargetSchema();

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public boolean isExecuting() {
        return executing;
    }

    public void setExecuting(boolean isExecuting) {
        this.executing = isExecuting;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean isExecutionCancelled) {
        this.cancelled = isExecutionCancelled;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public long getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(long executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public DBNConnection getConnection() {
        return connection;
    }

    public void setConnection(DBNConnection connection) {
        this.connection = connection;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public void reset() {
        timeout = 0;
        logging = false;
        executing = false;
        cancelled = false;
        executionTimestamp = 0;
        connection = null;
        statement = null;
    }
}
