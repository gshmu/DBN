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
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.*;

public class DBNConnection extends DBNConnectionBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private ConnectionType type;
    private ConnectionId id;

    private long lastAccess;
    private Set<DBNStatement> statements = new HashSet<DBNStatement>();
    private UncommittedChangeBundle dataChanges;

    public DBNConnection(Connection connection, ConnectionType type, ConnectionId id) {
        super(connection);
        this.type = type;
        this.id = id;
    }

    protected <S extends Statement> S wrap(S statement) {
        boolean wasEmpty = statements.isEmpty();
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
                if (TimeUtil.isOlderThan(currentStatement.getInitTimestamp(), TimeUtil.ONE_MINUTE)) {
                    LOGGER.error("Statement not released", new Throwable(currentStatement.traceable.getTrace()));
                }
            }
        }

        statements.add((DBNStatement) statement);
        if (wasEmpty) {
            statusChanged(ACTIVE);
        }
        return statement;
    }

    protected void release(DBNStatement statement) {
        statements.remove(statement);
        if (statements.isEmpty()) {
            statusChanged(ACTIVE);
        }
    }

    @Override
    public boolean isClosedInner() throws SQLException {
        return inner.isClosed();
    }

    @Override
    public void closeInner() throws SQLException {
        inner.close();
    }

    @Override
    public boolean isInvalidInner() throws SQLException {
        return !isActive() && !inner.isValid(2);
    }

    @Override
    public void invalidateInner() throws SQLException {
        // do nothing
    }

    public ConnectionId getId() {
        return id;
    }

    public ConnectionType getType() {
        return type;
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
    protected void statusChanged(ResourceStatus status) {
        ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(id);
        if (connectionHandler != null && !connectionHandler.isDisposed()) {
            ConnectionHandlerStatus connectionStatus = connectionHandler.getConnectionStatus();
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

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccess;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }

    /********************************************************************
     *                        Transaction                               *
     ********************************************************************/
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        try {
            super.setAutoCommit(autoCommit);
        } finally {
            set(AUTO_COMMIT, autoCommit);
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return super.getAutoCommit();
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
        return is(ACTIVE) || !statements.isEmpty();
    }

    public boolean isAutoCommit() {
        return is(AUTO_COMMIT);
    }

    public boolean set(ResourceStatus status, boolean value) {
        if (status == RESERVED && value && isActive()) {
            LOGGER.warn("Reserving active connection");
        }
        boolean changed = super.set(status, value);
        if (changed) statusChanged(status);
        return changed;
    }

    /********************************************************************
     *                             Data changes                         *
     ********************************************************************/
    public void notifyDataChanges(VirtualFile virtualFile) {
        if (!isAutoCommit()) {
            if (dataChanges == null) {
                dataChanges = new UncommittedChangeBundle();
            }
            dataChanges.notifyChange(virtualFile);
        }
    }

    public void resetDataChanges() {
        dataChanges = null;
    }

    @Nullable
    public UncommittedChangeBundle getDataChanges() {
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
