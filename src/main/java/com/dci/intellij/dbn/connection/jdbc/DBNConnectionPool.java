package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.exception.Exceptions;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.pool.ObjectPoolBase;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.connection.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.dci.intellij.dbn.common.notification.NotificationSupport.sendInfoNotification;

@Slf4j
public class DBNConnectionPool extends ObjectPoolBase<DBNConnection, SQLException> {
    private final ConnectionRef connection;
    private final AtomicLong lastAccess = new AtomicLong();

    public DBNConnectionPool(ConnectionHandler connection) {
        this.connection = ConnectionRef.of(connection);
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    public long getLastAccess() {
        return lastAccess.get();
    }

    public void updateLastAccess() {
        lastAccess.set(System.currentTimeMillis());
    }

    public final DBNConnection acquire(boolean readonly) throws SQLException {
        DBNConnection conn = acquire(30, TimeUnit.SECONDS);

        Resources.setAutoCommit(conn, readonly);
        Resources.setReadonly(getConnection(), conn, readonly);
        return conn;
    }


    @Override
    protected DBNConnection create() throws SQLException{
        checkDisposed();
        ConnectionHandler connection = getConnection();
        String connectionName = connection.getName();
        DBNConnection conn = ConnectionUtil.connect(connection, SessionId.POOL);

        Resources.setAutoCommit(conn, true);
        Resources.setReadonly(connection, conn, true);

        if (size() == 0) {
            // Notify first pool connection
            sendInfoNotification(
                    connection.getProject(),
                    NotificationGroup.SESSION,
                    "Connected to database \"{0}\"",
                    connection.getConnectionName(conn));
        }

        return conn;
    }

    @Override
    protected boolean check(@Nullable DBNConnection conn) {
        return conn != null && !conn.isClosed() && conn.isValid();
    }

    @Override
    public int maxSize() {
        return getConnection().getSettings().getDetailSettings().getMaxConnectionPoolSize();
    }

    @Override
    protected String identifier() {
        return getConnection().getName();
    }

    @Override
    protected String identifier(DBNConnection conn) {
        return "Connection " + (conn == null ? "" : conn.getResourceId());
    }

    @Override
    protected DBNConnection whenNull() throws SQLException{
        throw new SQLTimeoutException("Busy connection pool");
    }

    @Override
    protected DBNConnection whenErrored(Throwable e) throws SQLException{
        throw Exceptions.toSqlException(e);
    }

    @Override
    protected DBNConnection whenAcquired(DBNConnection conn) {
        conn.set(ResourceStatus.RESERVED, true);

        ConnectionHandler connection = getConnection();
        ConnectionHandlerStatusHolder connectionStatus = connection.getConnectionStatus();
        connectionStatus.setConnected(true);
        connectionStatus.setValid(true);
        updateLastAccess();

        return conn;
    }

    @Override
    protected DBNConnection whenReleased(DBNConnection conn) throws SQLException {
        Resources.rollback(conn);
        Resources.setAutoCommit(conn, true);
        Resources.setReadonly(getConnection(), conn, true);

        conn.set(ResourceStatus.RESERVED, false);
        updateLastAccess();
        return conn;
    }

    @Override
    protected DBNConnection whenDropped(DBNConnection conn) {
        Background.run(null, () -> Resources.close(conn));
        return conn;
    }
}
