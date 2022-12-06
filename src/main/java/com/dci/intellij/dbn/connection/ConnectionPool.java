package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.StatefulDisposableBase;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNConnectionCache;
import com.dci.intellij.dbn.connection.jdbc.DBNConnectionPool;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.dci.intellij.dbn.common.util.TimeUtil.isOlderThan;

@Slf4j
public final class ConnectionPool extends StatefulDisposableBase implements NotificationSupport, Disposable {
    static {
        ConnectionPoolCleaner.INSTANCE.start();
    }

    private final ConnectionRef connection;
    private final DBNConnectionPool connectionPool;
    private final DBNConnectionCache connectionCache;

/*    @Deprecated
    private final List<DBNConnection> oldConnectionPool = ContainerUtil.createLockFreeCopyOnWriteList();

    @Deprecated
    private final IntervalLoader<Long> lastAccessTimestamp = new IntervalLoader<Long>(TimeUtil.Millis.TEN_SECONDS) {
        @Override
        protected Long load() {
            if (oldConnectionPool.size() > 0) {
                long lastAccessTimestamp = 0;
                for (DBNConnection poolConnection : oldConnectionPool) {
                    if (poolConnection.getLastAccess() > lastAccessTimestamp) {
                        lastAccessTimestamp = poolConnection.getLastAccess();
                    }
                }

                return lastAccessTimestamp;
            } else {
                return Commons.nvl(getValue(), 0L);
            }
        }
    };*/

    ConnectionPool(@NotNull ConnectionHandler connection) {
        super(connection);
        this.connection = connection.ref();
        this.connectionCache = new DBNConnectionCache(connection);
        this.connectionPool = new DBNConnectionPool(connection);
    }

    DBNConnection ensureTestConnection() throws SQLException {
        return ensureConnection(SessionId.TEST);
    }

    @NotNull
    DBNConnection ensureMainConnection() throws SQLException {
        return ensureConnection(SessionId.MAIN);
    }

    @NotNull
    DBNConnection ensureDebugConnection() throws SQLException {
        return ensureConnection(SessionId.DEBUG);
    }

    @NotNull
    DBNConnection ensureDebuggerConnection() throws SQLException {
        return ensureConnection(SessionId.DEBUGGER);
    }

    @Nullable
    public DBNConnection getMainConnection() {
        return connectionCache.get(SessionId.MAIN);
    }

    @Nullable
    public DBNConnection getTestConnection() {
        return connectionCache.get(SessionId.TEST);
    }

    @Nullable
    public DBNConnection getSessionConnection(SessionId sessionId) {
        return connectionCache.get(sessionId);
    }

    @NotNull
    DBNConnection ensureSessionConnection(SessionId sessionId) throws SQLException {
        return ensureConnection(sessionId);
    }

    @NotNull
    public List<DBNConnection> getConnections(ConnectionType... connectionTypes) {
        List<DBNConnection> connections = new ArrayList<>();
        if (ConnectionType.POOL.matches(connectionTypes)) {
            connectionPool.visit(c -> connections.add(c));
        }

        connectionCache.visit(
                c -> c.getType().matches(connectionTypes),
                c -> connections.add(c));
        return connections;
    }

    @NotNull
    private DBNConnection ensureConnection(SessionId sessionId) throws SQLException {
        ConnectionHandler connection = getConnection();
        ConnectionManager.setLastUsedConnection(connection);
        return connectionCache.ensure(sessionId);
    }

    public void updateLastAccess() {
        connectionPool.updateLastAccess();
    }

    public long getLastAccess() {
        return connectionPool.getLastAccess();
    }

    boolean wasNeverAccessed() {
        return getLastAccess() == 0;
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    @Override
    @NotNull
    public Project getProject() {
        return getConnection().getProject();
    }

    @NotNull
    DBNConnection allocateConnection(boolean readonly) throws SQLException {
        return connectionPool.acquire(readonly);
    }

/*
    // TODO connection pool replacement
    @NotNull
    @Deprecated
    private DBNConnection allocateConnection(boolean readonly, int attempts) throws SQLException {
        ConnectionHandler connection = getConnection();
        ConnectionManager.setLastUsedConnection(connection);

        DBNConnection conn = lookupConnection();
        if (conn == null)  {
            ConnectionDetailSettings detailSettings = connection.getSettings().getDetailSettings();
            if (oldConnectionPool.size() >= detailSettings.getMaxConnectionPoolSize() && !ThreadMonitor.isDispatchThread()) {
                try {
                    if (attempts > 10) {
                        throw new SQLTimeoutException("Busy connection pool");
                    }
                    Thread.sleep(TimeUtil.Millis.TWO_SECONDS);
                    return allocateConnection(readonly, attempts + 1);
                } catch (SQLException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new SQLException("Could not allocate connection for '" + connection.getName() + "'. ", e);
                }
            }
            conn = createPoolConnection();
        }
        Resources.setReadonly(connection, conn, readonly);
        Resources.setAutoCommit(conn, readonly);
        return conn;
    }

    @Nullable
    @Deprecated
    private DBNConnection lookupConnection() {
        ConnectionHandler connection = getConnection();
        ConnectionHandlerStatusHolder statusHolder = connection.getConnectionStatus();

        for (DBNConnection conn : oldConnectionPool) {
            checkDisposed();
            if (!conn.isReserved() && !conn.isActive()) {
                synchronized (this) {
                    if (!conn.isReserved() && !conn.isActive()) {
                        conn.set(ResourceStatus.RESERVED, true);
                        if (!conn.isClosed() && conn.isValid()) {
                            statusHolder.setConnected(true);
                            statusHolder.setValid(true);
                            return conn;
                        } else {
                            oldConnectionPool.remove(conn);
                            Resources.close(conn);
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    @Deprecated
    private DBNConnection createPoolConnection() throws SQLException {
        checkDisposed();
        ConnectionHandler connection = getConnection();
        ConnectionHandlerStatusHolder connectionStatus = connection.getConnectionStatus();
        String connectionName = connection.getName();
        log.debug("[DBN] Attempt to create new pool connection for '" + connectionName + "'");
        DBNConnection conn = ConnectionUtil.connect(connection, SessionId.POOL);

        Resources.setAutoCommit(conn, true);
        Resources.setReadonly(connection, conn, true);
        connectionStatus.setConnected(true);
        connectionStatus.setValid(true);


        //connection.getConnectionBundle().notifyConnectionStatusListeners(connection);

        // pool connections do not need to have current schema set
        //connection.getDataDictionary().setTargetSchema(connection.getCurrentSchemaName(), connection);
        conn.set(ResourceStatus.RESERVED, true);

        if (oldConnectionPool.size() == 0) {
            // Notify first pool connection
            sendInfoNotification(
                    NotificationGroup.SESSION,
                    "Connected to database \"{0}\"",
                    connection.getConnectionName(conn));
        }

        oldConnectionPool.add(conn);
        int size = oldConnectionPool.size();
        if (size > peakPoolSize) peakPoolSize = size;
        log.debug("[DBN] Pool connection for '" + connectionName + "' created. Pool size = " + getSize());
        return conn;
    }
*/

    void releaseConnection(@Nullable DBNConnection connection) {
        if (connection == null) return;

        if (connection.isPoolConnection()) {
            connectionPool.release(connection);
        } else {
            log.error("Trying to release non-POOL connection: " + connection.getType(), new IllegalArgumentException("No POOL connection"));
        }

    }

    void closeConnections() {
        List<DBNConnection> connections = getConnections();
        for (DBNConnection connection : connections) {
            closeConnection(connection);
        }
    }

    void closeConnection(DBNConnection connection) {
        SessionId sessionId = connection.getSessionId();
        if (sessionId == SessionId.POOL) {
            connectionPool.drop(connection);
        } else {
            connectionCache.drop(sessionId);
        }
    }

    public int getSize() {
        return connectionPool.size();
    }

    public int getPeakPoolSize() {
        return connectionPool.peakSize();
    }

    @Override
    public void disposeInner() {
        Background.run(null, () -> closeConnections());
    }

    public boolean isConnected(SessionId sessionId) {

        if (sessionId == SessionId.POOL) {
            return connectionPool.size() > 0;
        }

        if (sessionId != null) {
            DBNConnection connection = connectionCache.get(sessionId);
            return connection != null && !connection.isClosed() && connection.isValid();
        }
        return false;
    }

    void clean() {
        if (connectionPool.isEmpty()) return;
        try {
            ConnectionHandler connection = getConnection();
            ConnectionHandlerStatusHolder status = connection.getConnectionStatus();
            if (status.is(ConnectionHandlerStatus.CLEANING)) return;

            try {
                status.set(ConnectionHandlerStatus.CLEANING, true);

                long lastAccess = getLastAccess();
                ConnectionDetailSettings detailSettings = connection.getSettings().getDetailSettings();
                int minutesToDisconnect = detailSettings.getIdleMinutesToDisconnectPool();
                if (lastAccess > 0 && isOlderThan(lastAccess, minutesToDisconnect, TimeUnit.MINUTES)) {
                    connectionPool.clean(conn -> !conn.isActive() && !conn.isReserved());
                }

            } finally {
                status.set(ConnectionHandlerStatus.CLEANING, false);
            }
        } catch (ProcessCanceledException ignore) {
        } catch (Exception e) {
            log.error("Failed to clean connection pool", e);
        }
    }
}
