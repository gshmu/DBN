package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IntervalLoader;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class ConnectionPool extends StatefulDisposable.Base implements NotificationSupport, Disposable {
    static {
        ConnectionPoolCleaner.INSTANCE.start();
    }

    private int peakPoolSize = 0;

    private final ConnectionRef connection;

    private final List<DBNConnection> poolConnections = ContainerUtil.createLockFreeCopyOnWriteList();
    private final Map<SessionId, DBNConnection> dedicatedConnections = new ConcurrentHashMap<>();

    private final IntervalLoader<Long> lastAccessTimestamp = new IntervalLoader<Long>(TimeUtil.Millis.TEN_SECONDS) {
        @Override
        protected Long load() {
            if (poolConnections.size() > 0) {
                long lastAccessTimestamp = 0;
                for (DBNConnection poolConnection : poolConnections) {
                    if (poolConnection.getLastAccess() > lastAccessTimestamp) {
                        lastAccessTimestamp = poolConnection.getLastAccess();
                    }
                }

                return lastAccessTimestamp;
            } else {
                return Commons.nvl(getValue(), 0L);
            }
        }
    };

    ConnectionPool(@NotNull ConnectionHandler connection) {
        super(connection);
        this.connection = connection.ref();
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
        return dedicatedConnections.get(SessionId.MAIN);
    }

    @Nullable
    public DBNConnection getTestConnection() {
        return dedicatedConnections.get(SessionId.TEST);
    }

    @Nullable
    public DBNConnection getSessionConnection(SessionId sessionId) {
        return dedicatedConnections.get(sessionId);
    }

    @NotNull
    DBNConnection ensureSessionConnection(SessionId sessionId) throws SQLException {
        return ensureConnection(sessionId);
    }

    @NotNull
    public List<DBNConnection> getConnections(ConnectionType... connectionTypes) {
        List<DBNConnection> connections = new ArrayList<>();
        if (ConnectionType.POOL.matches(connectionTypes)) {
            connections.addAll(poolConnections);
        }

        for (DBNConnection connection : dedicatedConnections.values()) {
            if (connection != null && connection.getType().matches(connectionTypes)) {
                connections.add(connection);
            }
        }
        return connections;
    }

    @NotNull
    private DBNConnection ensureConnection(SessionId sessionId) throws SQLException {
        DBNConnection conn = dedicatedConnections.get(sessionId);
        ConnectionHandler connection = getConnection();
        ConnectionManager.setLastUsedConnection(connection);

        if (shouldInit(conn)) {
            synchronized (this) {
                if (shouldInit(conn)) {
                    try {
                        Resources.close(conn);

                        conn = ConnectionUtil.connect(connection, sessionId);
                        dedicatedConnections.put(sessionId, conn);
                        sendInfoNotification(
                                NotificationGroup.SESSION,
                                "Connected to database \"{0}\"",
                                connection.getConnectionName(conn));
                    } finally {
                        ProjectEvents.notify(getProject(),
                                ConnectionStatusListener.TOPIC,
                                (listener) -> listener.statusChanged(connection.getConnectionId(), sessionId));
                    }
                }
            }
        }

        return conn;
    }

    private boolean shouldInit(DBNConnection connection) {
        return connection == null || connection.isClosed() || !connection.isValid();
    }

    public long getLastAccessTimestamp() {
        return lastAccessTimestamp.get();
    }

    boolean wasNeverAccessed() {
        return getLastAccessTimestamp() == 0;
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
        return allocateConnection(readonly, 0);
    }

    @NotNull
    private DBNConnection allocateConnection(boolean readonly, int attempts) throws SQLException {
        ConnectionHandler connection = getConnection();
        ConnectionManager.setLastUsedConnection(connection);

        DBNConnection conn = lookupConnection();
        if (conn == null)  {
            ConnectionDetailSettings detailSettings = connection.getSettings().getDetailSettings();
            if (poolConnections.size() >= detailSettings.getMaxConnectionPoolSize() && !ThreadMonitor.isDispatchThread()) {
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
    private DBNConnection lookupConnection() {
        ConnectionHandler connection = getConnection();
        ConnectionHandlerStatusHolder statusHolder = connection.getConnectionStatus();

        for (DBNConnection conn : poolConnections) {
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
                            poolConnections.remove(conn);
                            Resources.close(conn);
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
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

        if (poolConnections.size() == 0) {
            // Notify first pool connection
            sendInfoNotification(
                    NotificationGroup.SESSION,
                    "Connected to database \"{0}\"",
                    connection.getConnectionName(conn));
        }

        poolConnections.add(conn);
        int size = poolConnections.size();
        if (size > peakPoolSize) peakPoolSize = size;
        log.debug("[DBN] Pool connection for '" + connectionName + "' created. Pool size = " + getSize());
        return conn;
    }

    void releaseConnection(@Nullable DBNConnection connection) {
        if (connection != null) {
            if (connection.isPoolConnection()) {
                try {
                    Resources.rollback(connection);
                    Resources.setAutoCommit(connection, true);
                    Resources.setReadonly(getConnection(), connection, true);
                    connection.set(ResourceStatus.RESERVED, false);
                } catch (SQLException e) {
                    dropConnection(connection);
                }
            } else {
                log.error("Trying to release non-POOL connection: " + connection.getType(), new IllegalArgumentException("No POOL connection"));
            }

        }
    }

    void dropConnection(DBNConnection connection) {
        if (connection != null) {
            poolConnections.remove(connection);
            Resources.close(connection);
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
            poolConnections.remove(connection);
            Resources.close(connection);
        } else {
            dedicatedConnections.remove(sessionId);
            Resources.close(connection);
        }
    }

    public int getSize() {
        return poolConnections.size();
    }

    public int getPeakPoolSize() {
        return peakPoolSize;
    }

    @Override
    public void disposeInner() {
        Background.run(null, () -> closeConnections());
    }

    public boolean isConnected(SessionId sessionId) {

        if (sessionId == SessionId.POOL) {
            return poolConnections.size() > 0;
        }

        if (sessionId != null) {
            DBNConnection connection = dedicatedConnections.get(sessionId);
            return connection != null && !connection.isClosed() && connection.isValid();
        }
        return false;
    }

    void clean() {
        if (!poolConnections.isEmpty()) {
            try {
                ConnectionHandler connection = getConnection();
                ConnectionHandlerStatusHolder status = connection.getConnectionStatus();
                if (!status.is(ConnectionHandlerStatus.CLEANING)) {
                    try {
                        status.set(ConnectionHandlerStatus.CLEANING, true);

                        ConnectionDetailSettings detailSettings = connection.getSettings().getDetailSettings();
                        long lastAccessTimestamp = getLastAccessTimestamp();
                        if (lastAccessTimestamp > 0 && TimeUtil.isOlderThan(lastAccessTimestamp, detailSettings.getIdleMinutesToDisconnectPool(), TimeUnit.MINUTES)) {
                            // close connections only if pool is passive
                            for (DBNConnection conn : poolConnections) {
                                if (!conn.isIdle()) return;
                            }

                            List<DBNConnection> poolConnections = new ArrayList<>(this.poolConnections);
                            this.poolConnections.clear();

                            for (DBNConnection conn : poolConnections) {
                                Resources.close(conn);
                            }
                        }

                    } finally {
                        status.set(ConnectionHandlerStatus.CLEANING, false);
                    }
                }
            } catch (ProcessCanceledException ignore) {
            } catch (Exception e) {
                log.error("Failed to clean connection pool", e);
            }
        }
    }
}
