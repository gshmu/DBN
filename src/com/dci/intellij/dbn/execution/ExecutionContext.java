package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import com.dci.intellij.dbn.object.DBSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExecutionContext implements PropertyHolder<ExecutionStatus> {
    private transient int timeout;
    private transient boolean logging = false;
    private transient long executionTimestamp;
    private transient DBNConnection connection;
    private transient DBNStatement statement;
    private ExecutionStatusHolder status = new ExecutionStatusHolder();

    @Override
    public boolean set(ExecutionStatus status, boolean value) {
        return this.status.set(status, value);
    }

    @Override
    public boolean is(ExecutionStatus status) {
        return this.status.is(status);
    }

    @Override
    public boolean isNot(ExecutionStatus status) {
        return this.status.isNot(status);
    }

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

    public DBNStatement getStatement() {
        return statement;
    }

    public void setStatement(DBNStatement statement) {
        this.statement = statement;
    }

    public void reset() {
        status.reset();
        timeout = 0;
        logging = false;
        executionTimestamp = 0;
        connection = null;
        statement = null;

    }
}
