package com.dci.intellij.dbn.connection.jdbc;

import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.ACTIVE;
import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.RESERVED;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

public class DBNConnection extends DBNConnectionBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private ConnectionType type;
    private ConnectionId id;
    private SessionId sessionId;

    private long lastAccess = System.currentTimeMillis();
    private Set<DBNStatement> statements = new HashSet<DBNStatement>();
    private PendingTransactionBundle dataChanges;
    private String currentSchema;

    private IncrementalResourceStatusAdapter<DBNConnection> active =
            new IncrementalResourceStatusAdapter<DBNConnection>(ResourceStatus.ACTIVE, this) {
                @Override
                protected boolean setInner(ResourceStatus status, boolean value) {
                    return DBNConnection.super.set(status, value);
                }
            };

    private IncrementalResourceStatusAdapter<DBNConnection> reserved =
            new IncrementalResourceStatusAdapter<DBNConnection>(ResourceStatus.RESERVED, this) {
                @Override
                protected boolean setInner(ResourceStatus status, boolean value) {
                    return DBNConnection.super.set(status, value);
                }
            };

    private ResourceStatusAdapter<DBNConnection> invalid =
            new ResourceStatusAdapter<DBNConnection>(this,
                    ResourceStatus.INVALID,
                    ResourceStatus.INVALID_SETTING,
                    ResourceStatus.INVALID_CHECKING,
                    0,
                    true) { // true is terminal status
                @Override
                protected void changeInner(boolean value) throws SQLException {
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return !isActive() && !inner.isValid(2);
                }
            };

    private ResourceStatusAdapter<DBNConnection> autoCommit =
            new ResourceStatusAdapter<DBNConnection>(this,
                    ResourceStatus.AUTO_COMMIT,
                    ResourceStatus.AUTO_COMMIT_SETTING,
                    ResourceStatus.AUTO_COMMIT_CHECKING,
                    0,
                    false) { // no terminal status
                @Override
                protected void changeInner(boolean value) throws SQLException {
                    inner.setAutoCommit(value);
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return inner.getAutoCommit();
                }
            };

    public DBNConnection(Connection connection, ConnectionType type, ConnectionId id, SessionId sessionId) {
        super(connection);
        this.type = type;
        this.id = id;
        this.sessionId = sessionId;
    }

    protected <S extends Statement> S wrap(S statement) {
        updateLastAccess();
        if (statement instanceof CallableStatement) {
            CallableStatement callableStatement = (CallableStatement) statement;
            statement = (S) new DBNCallableStatement(callableStatement, this);

        } else  if (statement instanceof PreparedStatement) {
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            statement = (S) new DBNPreparedStatement(preparedStatement, this);

        } else {
            statement = (S) new DBNStatement<Statement>(statement, this);
        }

        if (isPoolConnection()) {
            for (DBNStatement currentStatement : statements) {
                if (TimeUtil.isOlderThan(currentStatement.getInitTimestamp(), TimeUtil.THREE_MINUTES)) {
                    LOGGER.error("Statement not released", new SQLException(currentStatement.traceable.getTrace()));
                }
            }
        }

        statements.add((DBNStatement) statement);
        return statement;
    }

    protected void release(DBNStatement statement) {
        statements.remove(statement);
        updateLastAccess();
    }

    @Override
    public boolean isClosedInner() throws SQLException {
        return inner.isClosed();
    }

    @Override
    public boolean isClosed() {
        // skip checking "closed" on active connections
        return !isActive() && super.isClosed();
    }

    @Override
    public void closeInner() throws SQLException {
        inner.close();
    }

    public ConnectionId getId() {
        return id;
    }

    public ConnectionType getType() {
        return type;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public boolean isPoolConnection() {
        return type == ConnectionType.POOL;
    }

    public boolean isDebugConnection() {
        return type == ConnectionType.DEBUG;
    }

    public boolean isDebuggerConnection() {
        return type == ConnectionType.DEBUGGER;
    }

    public boolean isMainConnection() {
        return type == ConnectionType.MAIN;
    }

    public boolean isTestConnection() {
        return type == ConnectionType.TEST;
    }

    @Override
    public void statusChanged(ResourceStatus status) {
        ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(id);
        if (connectionHandler != null && !connectionHandler.isDisposed()) {
            ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();
            switch (status) {
                case CLOSED: connectionStatus.getConnected().markDirty(); break;
                case INVALID: connectionStatus.getValid().markDirty(); break;
                case ACTIVE: connectionStatus.getActive().markDirty(); break;
            }
        }
    }

    public void updateLastAccess() {
        lastAccess = System.currentTimeMillis();
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccess;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }

    public static Connection getInner(Connection connection) {
        if (connection instanceof DBNConnection) {
            DBNConnection dbnConnection = (DBNConnection) connection;
            return dbnConnection.getInner();
        }
        return connection;
    }

    /********************************************************************
     *                        Transaction                               *
     ********************************************************************/
    @Override
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit.change(autoCommit);
    }

    @Override
    public boolean getAutoCommit() {
        return autoCommit.get();
    }

    @Override
    public void commit() throws SQLException {
        updateLastAccess();

        super.commit();
        resetDataChanges();
    }

    @Override
    public void rollback() throws SQLException {
        updateLastAccess();

        super.rollback();
        resetDataChanges();
    }

    @Override
    public void close() {
        updateLastAccess();

        super.close();
        resetDataChanges();
    }

    public String getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(String currentSchema) {
        this.currentSchema = currentSchema;
    }

    /********************************************************************
     *                             Status                               *
     ********************************************************************/
    public boolean isIdle() {
        return !isActive() && !isReserved();
    }

    public boolean isReserved() {
        return is(RESERVED);
    }

    public boolean isActive() {
        return is(ACTIVE);
    }


    public boolean isValid() {
        return isValid(2);
    }

    public boolean isValid(int timeout) {
        return !isInvalid();
    }

    public boolean isInvalid() {
        return invalid.get();
    }


    public boolean set(ResourceStatus status, boolean value) {
        boolean changed;
        if (status == ACTIVE) {
            changed = active.set(value);

        } else if (status == RESERVED) {
            if (value) {
                if (isActive()) {
                    LOGGER.warn("Reserving active connection");
                } else if (isReserved()) {
                    LOGGER.warn("Reserving already reserved connection");
                }
            }
            changed = reserved.set(value);
        } else {
            changed = super.set(status, value);
            if (changed) statusChanged(status);
        }


        return changed;
    }

    /********************************************************************
     *                             Data changes                         *
     ********************************************************************/
    public void notifyDataChanges(VirtualFile virtualFile) {
        if (!getAutoCommit()) {
            if (dataChanges == null) {
                dataChanges = new PendingTransactionBundle();
            }
            dataChanges.notifyChange(virtualFile);
        }
    }

    public void resetDataChanges() {
        dataChanges = null;
    }

    @Nullable
    public PendingTransactionBundle getDataChanges() {
        return dataChanges;
    }

    public boolean hasDataChanges() {
        return dataChanges != null && !dataChanges.isEmpty();
    }

    @Override
    public String toString() {
        return type + " - " + super.toString() + "";
    }
}
