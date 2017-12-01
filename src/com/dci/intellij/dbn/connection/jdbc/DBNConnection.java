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
import com.dci.intellij.dbn.common.util.InitializationInfo;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

public class DBNConnection extends DBNConnectionBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private ConnectionType type;

    private long lastAccess;
    private Set<DBNStatement> statements = new HashSet<>();
    private UncommittedChangeBundle dataChanges;

    public DBNConnection(Connection connection, ConnectionType type) {
        super(connection);
        this.type = type;
    }

    protected <S extends Statement> S wrap(S statement) {
        if (statement instanceof CallableStatement) {
            CallableStatement callableStatement = (CallableStatement) statement;
            statement = (S) new DBNCallableStatement(callableStatement, this);

        } else  if (statement instanceof PreparedStatement) {
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            statement = (S) new DBNPreparedStatement(preparedStatement, this);

        } else {
            statement = (S) new DBNStatement<>(statement, this);
        }

        if (isPoolConnection()) {
            for (DBNStatement currentStatement : statements) {
                InitializationInfo initInfo = currentStatement.initInfo;
                if (TimeUtil.isOlderThan(initInfo.getTimestamp(), TimeUtil.ONE_MINUTE)) {
                    LOGGER.error("Statement not released", initInfo.getStack());
                }
            }
        }

        statements.add((DBNStatement) statement);
        return statement;
    }

    protected void release(DBNStatement statement) {
        statements.remove(statement);
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
            set(ResourceStatus.AUTO_COMMIT, autoCommit);
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
        return is(ResourceStatus.RESERVED);
    }

    public boolean isActive() {
        return is(ResourceStatus.ACTIVE);
    }

    public boolean isAutoCommit() {
        return is(ResourceStatus.AUTO_COMMIT);
    }

    public boolean set(ResourceStatus status, boolean value) {
        if (status == ResourceStatus.RESERVED && value && isActive()) {
            LOGGER.warn("Reserving busy connection");
        }
        return super.set(status, value);
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
