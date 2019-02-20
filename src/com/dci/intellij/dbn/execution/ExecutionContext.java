package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.execution.ExecutionStatus.*;

public abstract class ExecutionContext extends PropertyHolderImpl<ExecutionStatus> implements PropertyHolder<ExecutionStatus> {
    private transient int timeout;
    private transient boolean logging = false;
    private transient long executionTimestamp;
    private transient DBNConnection connection;
    private transient DBNStatement statement;

    @Override
    protected ExecutionStatus[] properties() {
        return ExecutionStatus.values();
    }

    @NotNull
    public abstract String getTargetName();

    @Nullable
    public abstract ConnectionHandler getTargetConnection();

    @Nullable
    public abstract SchemaId getTargetSchema();

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

    public boolean canExecute() {
        return isNot(QUEUED) && isNot(EXECUTING) && isNot(CANCELLED);
    }

    @Override
    public void reset() {
        super.reset();
        timeout = 0;
        logging = false;
        executionTimestamp = System.currentTimeMillis();
        connection = null;
        statement = null;

    }
}
