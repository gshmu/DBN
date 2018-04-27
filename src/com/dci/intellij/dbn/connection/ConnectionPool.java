package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IntervalLoader;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;

public class ConnectionPool extends DisposableBase implements Disposable {

    private static Logger LOGGER = LoggerFactory.createLogger();
    private int peakPoolSize = 0;

    protected final Logger log = Logger.getInstance(getClass().getName());
    private ConnectionHandlerRef connectionHandlerRef;

    private List<DBNConnection> poolConnections = ContainerUtil.createLockFreeCopyOnWriteList();
    private Map<SessionId, DBNConnection> sessionConnections = ContainerUtil.newConcurrentMap();
    private DBNConnection mainConnection;
    private DBNConnection testConnection;
    private IntervalLoader<Long> lastAccessTimestamp = new IntervalLoader<Long>(TimeUtil.THIRTY_SECONDS) {
        @Override
        protected Long load() {
            long lastAccessTimestamp = 0;
            for (DBNConnection poolConnection : poolConnections) {
                if (poolConnection.getLastAccess() > lastAccessTimestamp) {
                    lastAccessTimestamp = poolConnection.getLastAccess();
                }
            }

            return lastAccessTimestamp;
        }
    };

    ConnectionPool(@NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler);
        this.connectionHandlerRef = connectionHandler.getRef();
        POOL_CLEANER_TASK.registerConnectionPool(this);
    }

    public DBNConnection ensureTestConnection() throws SQLException {
        testConnection = init(testConnection, ConnectionType.TEST);
        return testConnection;
    }

    @NotNull
    public DBNConnection ensureMainConnection() throws SQLException {
        mainConnection = init(mainConnection, ConnectionType.MAIN);
        return mainConnection;
    }

    @Nullable
    public DBNConnection getMainConnection() {
        return mainConnection;
    }

    @Nullable
    public DBNConnection getTestConnection() {
        return testConnection;
    }

    @Nullable
    public DBNConnection getSessionConnection(SessionId sessionId) {
        if (sessionId == SessionId.MAIN) {
            return mainConnection;
        } if (sessionId != SessionId.POOL) {
            return sessionConnections.get(sessionId);
        }
        return null;
    }

    @NotNull
    public DBNConnection ensureSessionConnection(SessionId sessionId) throws SQLException {
        DBNConnection connection = sessionConnections.get(sessionId);
        connection = init(connection, ConnectionType.SESSION);
        sessionConnections.put(sessionId, connection);
        return connection;
    }

    @NotNull
    public List<DBNConnection> getConnections(ConnectionType... connectionTypes) {
        ArrayList<DBNConnection> connections = new ArrayList<DBNConnection>();
        if (isOneOf(ConnectionType.MAIN, connectionTypes) && mainConnection != null) {
            connections.add(mainConnection);
        }

        if (isOneOf(ConnectionType.TEST, connectionTypes) && testConnection != null) {
            connections.add(testConnection);
        }

        if (isOneOf(ConnectionType.POOL, connectionTypes)) {
            connections.addAll(poolConnections);
        }
        if (isOneOf(ConnectionType.SESSION, connectionTypes)) {
            for (DBNConnection connection : sessionConnections.values()) {
                if (connection != null) {
                    connections.add(connection);
                }
            }
        }
        return connections;
    }

    private static boolean isOneOf(ConnectionType connectionType, ConnectionType... connectionTypes) {
        if (connectionTypes == null || connectionTypes.length == 0) return true;
        for (ConnectionType type : connectionTypes) {
            if (connectionType == type) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private DBNConnection init(DBNConnection connection, ConnectionType connectionType) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.setLastUsedConnection(connectionHandler);

        if (shouldInit(connection)) {
            synchronized (this) {
                if (shouldInit(connection)) {
                    try {
                        ConnectionUtil.close(connection);

                        connection = ConnectionUtil.connect(connectionHandler, connectionType);
                        NotificationUtil.sendInfoNotification(
                                getProject(),
                                Constants.DBN_TITLE_PREFIX + "Connect",
                                "Connected to database \"{0}\"",
                                connectionHandler.getName());
                    } finally {
                        ConnectionHandlerStatusListener changeListener = EventUtil.notify(getProject(), ConnectionHandlerStatusListener.TOPIC);
                        changeListener.statusChanged(connectionHandler.getId(), ConnectionHandlerStatus.CONNECTED);
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

    public boolean wasNeverAccessed() {
        return getLastAccessTimestamp() == 0;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @NotNull
    private Project getProject() {
        return getConnectionHandler().getProject();
    }

    @NotNull
    public DBNConnection allocateConnection(boolean readonly) throws SQLException {
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
            connection = createConnection();
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
    private DBNConnection createConnection() throws SQLException {
        checkDisposed();
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();
        String connectionName = connectionHandler.getName();
        LOGGER.debug("[DBN-INFO] Attempt to create new pool connection for '" + connectionName + "'");
        DBNConnection connection = ConnectionUtil.connect(connectionHandler, ConnectionType.POOL);
        ConnectionUtil.setAutoCommit(connection, true);
        ConnectionUtil.setReadonly(connection, true);
        connectionStatus.setConnected(true);
        connectionStatus.setValid(true);


        //connectionHandler.getConnectionBundle().notifyConnectionStatusListeners(connectionHandler);

        // pool connections do not need to have current schema set
        //connectionHandler.getDataDictionary().setTargetSchema(connectionHandler.getCurrentSchemaName(), connection);
        connection.set(ResourceStatus.RESERVED, true);

        poolConnections.add(connection);
        int size = poolConnections.size();
        if (size > peakPoolSize) peakPoolSize = size;
        LOGGER.debug("[DBN-INFO] Pool connection for '" + connectionName + "' created. Pool size = " + getSize());
        return connection;
    }

    public void releaseConnection(DBNConnection connection) {
        if (connection != null) {
            ConnectionUtil.rollback(connection);
            ConnectionUtil.setAutocommit(connection, true);
            ConnectionUtil.setReadonly(connection, true);
            connection.set(ResourceStatus.RESERVED, false);
        }
    }

    public void dropConnection(DBNConnection connection) {
        if (connection != null) {
            poolConnections.remove(connection);
            ConnectionUtil.close(connection);
        }
    }

    public void closeConnections() {
        for (DBNConnection connection : poolConnections) {
            ConnectionUtil.close(connection);
        }
        poolConnections.clear();

        for (SessionId sessionId : sessionConnections.keySet()) {
            DBNConnection connection = sessionConnections.remove(sessionId);
            ConnectionUtil.close(connection);
        }

        mainConnection = ConnectionUtil.close(mainConnection);
        testConnection = ConnectionUtil.close(testConnection);
    }

    @Deprecated
    public void keepAlive(boolean check) {
        if (mainConnection != null) {
            mainConnection.updateLastAccess();
            if (check) mainConnection.isValid();
        }
    }

    public int getSize() {
        return poolConnections.size();
    }

    public List<DBNConnection> getPoolConnections() {
        return poolConnections;
    }

    public Map<SessionId, DBNConnection> getSessionConnections() {
        return sessionConnections;
    }

    public int getPeakPoolSize() {
        return peakPoolSize;
    }

    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            closeConnections();
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (mainConnection != null && !mainConnection.isClosed()) {
            mainConnection.setAutoCommit(autoCommit);
        }
    }

    public boolean isConnected(SessionId sessionId) {

        if (sessionId == SessionId.POOL) {
            return poolConnections.size() > 0;
        }

        DBNConnection connection =
                sessionId == SessionId.MAIN ?
                        mainConnection :
                        sessionConnections.get(sessionId);

        return connection != null && !connection.isClosed() && connection.isValid();
    }

    private static class ConnectionPoolCleanTask extends TimerTask {
        List<WeakReference<ConnectionPool>> connectionPools = new CopyOnWriteArrayList<WeakReference<ConnectionPool>>();

        public void run() {
            for (WeakReference<ConnectionPool> connectionPoolRef : connectionPools) {
                ConnectionPool connectionPool = connectionPoolRef.get();
                if (connectionPool != null) {
                    ConnectionDetailSettings detailSettings = connectionPool.getConnectionHandler().getSettings().getDetailSettings();
                    long lastAccessTimestamp = connectionPool.getLastAccessTimestamp();
                    if (TimeUtil.isOlderThan(lastAccessTimestamp, detailSettings.getIdleTimeToDisconnectPool())) {
                        // close connections only if pool is passive
                        for (DBNConnection connection : connectionPool.poolConnections) {
                            if (!connection.isIdle()) return;
                        }

                        for (DBNConnection connection : connectionPool.poolConnections) {
                            ConnectionUtil.close(connection);
                        }
                        connectionPool.poolConnections.clear();
                    }
                }
            }
        }

        public void registerConnectionPool(ConnectionPool connectionPool) {
            connectionPools.add(new WeakReference<ConnectionPool>(connectionPool));
        }
    }

    private static ConnectionPoolCleanTask POOL_CLEANER_TASK = new ConnectionPoolCleanTask();
    static {
        Timer poolCleaner = new Timer("DBN - Connection Pool Cleaner");
        poolCleaner.schedule(POOL_CLEANER_TASK, TimeUtil.ONE_MINUTE, TimeUtil.ONE_MINUTE);
    }
}
