package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.object.DBSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class ExecutionContext {
    private transient int timeout;
    private transient boolean logging = false;
    private transient boolean executing = false;
    private transient boolean executionCancelled = false;
    private transient long executionTimestamp;
    private transient SQLException executionException;
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

    public boolean isExecutionCancelled() {
        return executionCancelled;
    }

    public void setExecutionCancelled(boolean isExecutionCancelled) {
        this.executionCancelled = isExecutionCancelled;
    }

    public long getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(long executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public SQLException getExecutionException() {
        return executionException;
    }

    public void setExecutionException(SQLException executionException) {
        this.executionException = executionException;
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

    public void resetStatement() {
        ConnectionUtil.cancelStatement(statement);
        ConnectionUtil.closeStatement(statement);
    }

    public void reset() {
        timeout = 0;
        logging = false;
        executing = false;
        executionCancelled = false;
        executionTimestamp = 0;
        executionException = null;
        connection = null;
        statement = null;
    }
}
