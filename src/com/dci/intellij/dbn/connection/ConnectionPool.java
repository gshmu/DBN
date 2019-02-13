package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IntervalLoader;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ConnectionPool extends DisposableBase implements NotificationSupport, Disposable {

    private static Logger LOGGER = LoggerFactory.createLogger();
    private int peakPoolSize = 0;

    protected final Logger log = Logger.getInstance(getClass().getName());
    private ConnectionHandlerRef connectionHandlerRef;

    private List<DBNConnection> poolConnections = ContainerUtil.createLockFreeCopyOnWriteList();
    private Map<SessionId, DBNConnection> dedicatedConnections = ContainerUtil.newConcurrentMap();

    private IntervalLoader<Long> lastAccessTimestamp = new IntervalLoader<Long>(TimeUtil.TEN_SECONDS) {
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
                return CommonUtil.nvl(getValue(), 0L);
            }
        }
    };

    ConnectionPool(@NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler);
        this.connectionHandlerRef = connectionHandler.getRef();
        POOL_CLEANER_TASK.registerConnectionPool(this);
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

    private static DBNConnection verify(DBNConnection connection) {
        if (connection != null) {
            if (!connection.isActive() && !connection.isReserved() && !connection.isValid()){
                return null;
            }
        }
        return connection;
    }

    @NotNull
    DBNConnection ensureSessionConnection(SessionId sessionId) throws SQLException {
        return ensureConnection(sessionId);
    }

    @NotNull
    public List<DBNConnection> getConnections(ConnectionType... connectionTypes) {
        ArrayList<DBNConnection> connections = new ArrayList<>();
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
        DBNConnection connection = dedicatedConnections.get(sessionId);
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.setLastUsedConnection(connectionHandler);

        if (shouldInit(connection)) {
            synchronized (this) {
                if (shouldInit(connection)) {
                    try {
                        ConnectionUtil.close(connection);

                        connection = ConnectionUtil.connect(connectionHandler, sessionId);
                        dedicatedConnections.put(sessionId, connection);
                        sendInfoNotification(
                                Constants.DBN_TITLE_PREFIX + "Session",
                                "Connected to database \"{0}\"",
                                connectionHandler.getConnectionName(connection));
                    } finally {
                        ConnectionStatusListener changeListener = EventUtil.notify(getProject(), ConnectionStatusListener.TOPIC);
                        changeListener.statusChanged(connectionHandler.getConnectionId(), sessionId);
                    }
                }
            }
        }

        return connection;
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
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.getnn();
    }

    @Override
    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @NotNull
    DBNConnection allocateConnection(boolean readonly) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.setLastUsedConnection(connectionHandler);

        DBNConnection connection = lookupConnection();
        if (connection == null)  {
            ConnectionDetailSettings detailSettings = connectionHandler.getSettings().getDetailSettings();
            if (poolConnections.size() >= detailSettings.getMaxConnectionPoolSize() && !ApplicationManager.getApplication().isDispatchThread()) {
                try {
                    Thread.sleep(TimeUtil.ONE_SECOND);
                    return allocateConnection(readonly);
                } catch (InterruptedException e) {
                    throw new SQLException("Could not allocate connection for '" + connectionHandler.getName() + "'. ");
                }
            }
            connection = createPoolConnection();
        }
        ConnectionUtil.setReadonly(connection, readonly);
        ConnectionUtil.setAutoCommit(connection, readonly);
        return connection;
    }

    @Nullable
    private DBNConnection lookupConnection() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();

        for (DBNConnection connection : poolConnections) {
            checkDisposed();
            if (!connection.isReserved() && !connection.isActive()) {
                synchronized (this) {
                    if (!connection.isReserved() && !connection.isActive()) {
                        connection.set(ResourceStatus.RESERVED, true);
                        if (!connection.isClosed() && connection.isValid()) {
                            connectionStatus.setConnected(true);
                            connectionStatus.setValid(true);
                            return connection;
                        } else {
                            poolConnections.remove(connection);
                            ConnectionUtil.close(connection);
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
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();
        String connectionName = connectionHandler.getName();
        LOGGER.debug("[DBN-INFO] Attempt to create new pool connection for '" + connectionName + "'");
        DBNConnection connection = ConnectionUtil.connect(connectionHandler, SessionId.POOL);

        ConnectionUtil.setAutoCommit(connection, true);
        ConnectionUtil.setReadonly(connection, true);
        connectionStatus.setConnected(true);
        connectionStatus.setValid(true);


        //connectionHandler.getConnectionBundle().notifyConnectionStatusListeners(connectionHandler);

        // pool connections do not need to have current schema set
        //connectionHandler.getDataDictionary().setTargetSchema(connectionHandler.getCurrentSchemaName(), connection);
        connection.set(ResourceStatus.RESERVED, true);

        if (poolConnections.size() == 0) {
            // Notify first pool connection
            sendInfoNotification(
                    Constants.DBN_TITLE_PREFIX + "Session",
                    "Connected to database \"{0}\"",
                    connectionHandler.getConnectionName(connection));
        }

        poolConnections.add(connection);
        int size = poolConnections.size();
        if (size > peakPoolSize) peakPoolSize = size;
        LOGGER.debug("[DBN-INFO] Pool connection for '" + connectionName + "' created. Pool size = " + getSize());
        return connection;
    }

    void releaseConnection(DBNConnection connection) {
        if (connection != null) {
            if (connection.isPoolConnection()) {
                try {
                    ConnectionUtil.rollback(connection);
                    ConnectionUtil.setAutoCommit(connection, true);
                    ConnectionUtil.setReadonly(connection, true);
                    connection.set(ResourceStatus.RESERVED, false);
                } catch (SQLException e) {
                    dropConnection(connection);
                }
            } else {
                LOGGER.error("Trying to release non-POOL connection: " + connection.getType(), new IllegalArgumentException("No POOL connection"));
            }

        }
    }

    void dropConnection(DBNConnection connection) {
        if (connection != null) {
            poolConnections.remove(connection);
            ConnectionUtil.close(connection);
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
            ConnectionUtil.close(connection);
        } else {
            dedicatedConnections.remove(sessionId);
            ConnectionUtil.close(connection);
        }
    }

    public int getSize() {
        return poolConnections.size();
    }

    public List<DBNConnection> getPoolConnections() {
        return poolConnections;
    }

    public Map<SessionId, DBNConnection> getDedicatedConnections() {
        return dedicatedConnections;
    }

    public int getPeakPoolSize() {
        return peakPoolSize;
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            closeConnections();
        }
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

    private static class ConnectionPoolCleanTask extends TimerTask {
        List<WeakRef<ConnectionPool>> connectionPools = CollectionUtil.createConcurrentList();

        @Override
        public void run() {
            for (WeakRef<ConnectionPool> connectionPoolRef : connectionPools) {
                ConnectionPool connectionPool = connectionPoolRef.get();
                if (connectionPool != null && !connectionPool.poolConnections.isEmpty()) {
                    try {
                        ConnectionHandler connectionHandler = connectionPool.getConnectionHandler();
                        ConnectionHandlerStatusHolder status = connectionHandler.getConnectionStatus();
                        if (!status.is(ConnectionHandlerStatus.CLEANING)) {
                            try {
                                status.set(ConnectionHandlerStatus.CLEANING, true);

                                ConnectionDetailSettings detailSettings = connectionHandler.getSettings().getDetailSettings();
                                long lastAccessTimestamp = connectionPool.getLastAccessTimestamp();
                                if (lastAccessTimestamp > 0 && TimeUtil.isOlderThan(lastAccessTimestamp, detailSettings.getIdleTimeToDisconnectPool(), TimeUnit.MINUTES)) {
                                    // close connections only if pool is passive
                                    for (DBNConnection connection : connectionPool.poolConnections) {
                                        if (!connection.isIdle()) return;
                                    }

                                    List<DBNConnection> poolConnections = new ArrayList<>(connectionPool.poolConnections);
                                    connectionPool.poolConnections.clear();

                                    for (DBNConnection connection : poolConnections) {
                                        ConnectionUtil.close(connection);
                                    }
                                }

                            } finally {
                                status.set(ConnectionHandlerStatus.CLEANING, false);
                            }
                        }
                    } catch (Exception ignore) {}
                }
            }
        }

        void registerConnectionPool(ConnectionPool connectionPool) {
            connectionPools.add(WeakRef.from(connectionPool));
        }
    }

    private static ConnectionPoolCleanTask POOL_CLEANER_TASK = new ConnectionPoolCleanTask();
    static {
        Timer poolCleaner = new Timer("DBN - Idle Connection Pool Cleaner");
        poolCleaner.schedule(POOL_CLEANER_TASK, TimeUtil.ONE_MINUTE, TimeUtil.ONE_MINUTE);
    }
}
