package com.dci.intellij.dbn.connection.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.intellij.openapi.diagnostic.Logger;

public class DBNConnection extends DBNConnectionBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private ConnectionType type;

    private Set<DBNStatement> statements = new HashSet<>();
    private ResourceStatusMonitor statusMonitor = new ResourceStatusMonitor() {
        @Override
        protected boolean checkValid(int timeout) throws SQLException {
            return inner.isValid(timeout);
        }
    };

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
        for (DBNStatement currentStatement : statements) {
            LOGGER.error("Statement not released", currentStatement.getInitStack());
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

    public boolean isIdle() {
        return statements.isEmpty();
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

    public boolean iTestConnection() {
        return type == ConnectionType.TEST;
    }

    /****************************************************************************
     *                       Status utilities                                   *
     ****************************************************************************/
    public boolean isValid() {
        return statusMonitor.isValid();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return statusMonitor.isValid(timeout);
    }

    public void setBusy(boolean busy) {
        statusMonitor.setBusy(busy);
    }

    public boolean isBusy() {
        return statusMonitor.isBusy();
    }

    public void keepAlive() {
        statusMonitor.keepAlive();
    }

    public int getIdleMinutes() {
        return statusMonitor.getIdleMinutes();
    }
}
