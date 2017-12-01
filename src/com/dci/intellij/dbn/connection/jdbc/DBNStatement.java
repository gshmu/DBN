package com.dci.intellij.dbn.connection.jdbc;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.concurrent.Callable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.openapi.diagnostic.Logger;

public class DBNStatement<T extends Statement> extends DBNResource implements Statement, Closeable, Cancellable {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    protected T inner;
    protected SQLException exception;

    private WeakReference<DBNConnection> connection;
    private WeakReference<DBNResultSet> resultSet;


    DBNStatement(T inner, DBNConnection connection) {
        super(ResourceType.STATEMENT);
        this.inner = inner;
        this.connection = new WeakReference<>(connection);
    }

    @Override
    public DBNConnection getConnection() {
        return FailsafeUtil.get(connection.get());
    }


    @Override
    public boolean isCancelledInner() throws SQLException {
        return false;
    }

    @Override
    public void cancelInner() throws SQLException {
        inner.cancel();
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
    public void close() {
        try {
            super.close();
        } finally {
            DBNConnection connection = this.connection.get();
            if (connection != null) {
                connection.release(this);
            }
        }
    }

    protected DBNResultSet wrap(ResultSet original) {
        if (original == null) {
            resultSet = null;
        } else {
            if (resultSet == null) {
                resultSet = new WeakReference<>(new DBNResultSet(original, this));
            } else {
                DBNResultSet wrapped = resultSet.get();
                if (wrapped == null || wrapped.inner != original) {
                    resultSet = new WeakReference<>(new DBNResultSet(original, this));
                }
            }
        }
        return this.resultSet == null ? null : this.resultSet.get();
    }

    protected Object wrap(Object object) {
        if (object instanceof ResultSet) {
            ResultSet resultSet = (ResultSet) object;
            return new DBNResultSet(resultSet, getConnection());
        }
        return object;
    }

    protected abstract class ManagedExecutor<R> implements Callable<R> {
        @Override
        public R call() throws SQLException {
            DBNConnection connection = getConnection();
            ConnectionStatusMonitor statusMonitor = connection.getStatusMonitor();
            statusMonitor.updateLastAccess();

            boolean wasActive = connection.is(ConnectionProperty.ACTIVE);
            if (wasActive) {
                LOGGER.warn("Connection already busy with another statement");
            }
            try {
                connection.set(ConnectionProperty.ACTIVE, true);
                return execute();
            } catch (SQLException e) {
                exception = e;
                throw exception;
            } finally {
                statusMonitor.updateLastAccess();
                connection.set(ConnectionProperty.ACTIVE, wasActive);
            }
        }
        protected abstract R execute() throws SQLException;
    }


    /********************************************************************
     *                     Wrapped executions                           *
     ********************************************************************/
    @Override
    public boolean execute(final String sql) throws SQLException {
        return new ManagedExecutor<Boolean>() {
            @Override
            protected Boolean execute() throws SQLException {
                return inner.execute(sql);
            }
        }.call();
    }

    @Override
    public DBNResultSet executeQuery(final String sql) throws SQLException {
        return new ManagedExecutor<DBNResultSet>() {
            @Override
            protected DBNResultSet execute() throws SQLException {
                return wrap(inner.executeQuery(sql));
            }
        }.call();
    }

    @Override
    public int executeUpdate(final String sql) throws SQLException {
        return new ManagedExecutor<Integer>() {
            @Override
            protected Integer execute() throws SQLException {
                return inner.executeUpdate(sql);
            }
        }.call();
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        return new ManagedExecutor<Integer>() {
            @Override
            protected Integer execute() throws SQLException {
                return inner.executeUpdate(sql, autoGeneratedKeys);
            }
        }.call();
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        return new ManagedExecutor<Integer>() {
            @Override
            protected Integer execute() throws SQLException {
                return inner.executeUpdate(sql, columnIndexes);
            }
        }.call();
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        return new ManagedExecutor<Integer>() {
            @Override
            protected Integer execute() throws SQLException {
                return inner.executeUpdate(sql, columnNames);
            }
        }.call();
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        return new ManagedExecutor<Boolean>() {
            @Override
            protected Boolean execute() throws SQLException {
                return inner.execute(sql, autoGeneratedKeys);
            }
        }.call();
    }

    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        return new ManagedExecutor<Boolean>() {
            @Override
            protected Boolean execute() throws SQLException {
                return inner.execute(sql, columnIndexes);
            }
        }.call();
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        return new ManagedExecutor<Boolean>() {
            @Override
            protected Boolean execute() throws SQLException {
                return inner.execute(sql, columnNames);
            }
        }.call();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new ManagedExecutor<int[]>() {
            @Override
            protected int[] execute() throws SQLException {
                return inner.executeBatch();
            }
        }.call();
    }


    /********************************************************************
     *                     Wrapped functionality                        *
     ********************************************************************/
    @Override
    public int getMaxFieldSize() throws SQLException {
        return inner.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        inner.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return inner.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        inner.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        inner.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return inner.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        inner.setQueryTimeout(seconds);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return inner.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        inner.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        inner.setCursorName(name);
    }

    @Override
    public DBNResultSet getResultSet() throws SQLException {
        return wrap(inner.getResultSet());
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return inner.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return inner.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        inner.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return inner.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        inner.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return inner.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return inner.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return inner.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        inner.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        inner.clearBatch();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return inner.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return inner.getGeneratedKeys();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return inner.getResultSetHoldability();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        inner.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return inner.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        inner.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return inner.isCloseOnCompletion();
    }


    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return inner.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return inner.isWrapperFor(iface);
    }
}
