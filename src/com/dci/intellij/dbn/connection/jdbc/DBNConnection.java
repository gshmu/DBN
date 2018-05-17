package com.dci.intellij.dbn.connection.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusHolder;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.ACTIVE;
import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.RESERVED;

public class DBNConnection extends DBNConnectionBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private ConnectionType type;
    private ConnectionId id;
    private String sessionName;

    private long lastAccess;
    private Set<DBNStatement> statements = new HashSet<DBNStatement>();
    private PendingTransactionBundle dataChanges;

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
                    TimeUtil.THIRTY_SECONDS) {
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
                    TimeUtil.FIVE_MINUTES) {
                @Override
                protected void changeInner(boolean value) throws SQLException {
                    inner.setAutoCommit(value);
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return inner.getAutoCommit();
                }

                @Override
                protected void fail() {
                    // do not set the status if check failed
                }
            };

    public DBNConnection(Connection connection, ConnectionType type, ConnectionId id) {
        super(connection);
        this.type = type;
        this.id = id;
        this.sessionName = type.getName();
    }

    protected <S extends Statement> S wrap(S statement) {
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
    public void closeInner() throws SQLException {
        inner.close();
    }

    public ConnectionId getId() {
        return id;
    }

    public ConnectionType getType() {
        return type;
    }


    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public boolean isPoolConnection() {
        return type == ConnectionType.POOL;
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

    /********************************************************************
     *                        Transaction                               *
     ********************************************************************/
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit.change(autoCommit);
    }

    @Override
    public boolean getAutoCommit() {
        return autoCommit.get();
    }

    @Override
    public void commit() throws SQLException {
        super.commit();
        resetDataChanges();
    }

    @Override
    public void rollback() throws SQLException {
        super.rollback();
        resetDataChanges();
    }

    @Override
    public void close() {
        super.close();
        resetDataChanges();
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
