package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public class ConnectionPool extends DisposableBase implements Disposable {

    private static Logger LOGGER = LoggerFactory.createLogger();
    private long lastAccessTimestamp = 0;
    private int peakPoolSize = 0;

    protected final Logger log = Logger.getInstance(getClass().getName());
    private ConnectionHandlerRef connectionHandlerRef;

    private List<DBNConnection> poolConnections = new CopyOnWriteArrayList<DBNConnection>();
    private DBNConnection mainConnection;
    private DBNConnection testConnection;

    ConnectionPool(@NotNull ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
        POOL_CLEANER_TASK.registerConnectionPool(this);
    }

    public DBNConnection getTestConnection() throws SQLException {
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
        // TODO add session connections
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
        lastAccessTimestamp = System.currentTimeMillis();
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
                        notifyStatusChange();
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
        return lastAccessTimestamp;
    }

    private void notifyStatusChange() {
        ConnectionStatusListener changeListener = EventUtil.notify(getProject(), ConnectionStatusListener.TOPIC);
        changeListener.statusChanged(getConnectionHandler().getId());
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
        lastAccessTimestamp = System.currentTimeMillis();
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
        ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();

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
        ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
        String connectionName = connectionHandler.getName();
        LOGGER.debug("[DBN-INFO] Attempt to create new pool connection for '" + connectionName + "'");
        DBNConnection connection = ConnectionUtil.connect(connectionHandler, ConnectionType.POOL);
        ConnectionUtil.setAutoCommit(connection, true);
        ConnectionUtil.setReadonly(connection, true);
        connectionStatus.setConnected(true);
        connectionStatus.setValid(true);


        //connectionHandler.getConnectionBundle().notifyConnectionStatusListeners(connectionHandler);

        // pool connections do not need to have current schema set
        //connectionHandler.getDataDictionary().setCurrentSchema(connectionHandler.getCurrentSchemaName(), connection);
        connection.set(ResourceStatus.RESERVED, true);

        poolConnections.add(connection);
        int size = poolConnections.size();
        if (size > peakPoolSize) peakPoolSize = size;
        lastAccessTimestamp = System.currentTimeMillis();
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
        lastAccessTimestamp = System.currentTimeMillis();
    }

    public void dropConnection(DBNConnection connection) {
        if (connection != null) {
            poolConnections.remove(connection);
            ConnectionUtil.close(connection);
        }
        lastAccessTimestamp = System.currentTimeMillis();
    }

    public void closeConnectionsSilently() {
        for (DBNConnection connection : poolConnections) {
            ConnectionUtil.close(connection);
        }
        poolConnections.clear();

        ConnectionUtil.close(mainConnection);
        mainConnection = null;
    }

    public void closeConnections() {
        for (DBNConnection connection : new ArrayList<>(poolConnections)) {
            ConnectionUtil.close(connection);
        }
        poolConnections.clear();

        mainConnection = ConnectionUtil.close(mainConnection);
        testConnection = ConnectionUtil.close(testConnection);
    }

    @Deprecated
    public int getIdleMinutes() {
        return mainConnection == null ? 0 : mainConnection.getIdleMinutes();
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

    public int getPeakPoolSize() {
        return peakPoolSize;
    }

    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            closeConnectionsSilently();
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (mainConnection != null && !mainConnection.isClosed()) {
            mainConnection.setAutoCommit(autoCommit);
        }
    }

    private static class ConnectionPoolCleanTask extends TimerTask {
        List<WeakReference<ConnectionPool>> connectionPools = new CopyOnWriteArrayList<WeakReference<ConnectionPool>>();

        public void run() {
            for (WeakReference<ConnectionPool> connectionPoolRef : connectionPools) {
                ConnectionPool connectionPool = connectionPoolRef.get();
                if (connectionPool != null && TimeUtil.isOlderThan(connectionPool.lastAccessTimestamp, TimeUtil.ONE_MINUTE)) {
                    // close connections only if pool is passive
                    for (DBNConnection connection : connectionPool.poolConnections) {
                        if (connection.isActive()) return;
                    }

                    for (DBNConnection connection : connectionPool.poolConnections) {
                        ConnectionUtil.close(connection);
                    }
                    connectionPool.poolConnections.clear();
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
