package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public class ConnectionPool implements Disposable {

    private static Logger LOGGER = LoggerFactory.createLogger();
    private long lastAccessTimestamp = 0;
    private int peakPoolSize = 0;
    private boolean isDisposed;

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
        testConnection = init(testConnection, ConnectionType.TEST, true);
        return testConnection;
    }

    @Nullable
    public DBNConnection getMainConnection(boolean forceInit) throws SQLException {
        mainConnection = init(mainConnection, ConnectionType.MAIN, forceInit);
        return mainConnection;
    }

    private DBNConnection init(DBNConnection connection, ConnectionType connectionType, boolean force) throws SQLException {
        lastAccessTimestamp = System.currentTimeMillis();
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.setLastUsedConnection(connectionHandler);

        if (shouldInit(connection, force)) {
            synchronized (this) {
                if (shouldInit(connection, force)) {
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

    private boolean shouldInit(DBNConnection connection, boolean force) throws SQLException {
        return force && (connection == null || connection.isClosed() || !connection.isValid(2));
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
    private DBNConnection lookupConnection() throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();

        for (DBNConnection connection : poolConnections) {
            if (!connection.isReserved()) {
                if (!connection.isClosed() && connection.isValid()) {
                    connection.setReserved(true);
                    connectionStatus.setConnected(true);
                    connectionStatus.setValid(true);
                    return connection;
                } else {
                    poolConnections.remove(connection);
                    ConnectionUtil.close(connection);
                }
            }
        }
        return null;
    }

    @NotNull
    private DBNConnection createConnection() throws SQLException {
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
        connection.setReserved(true);
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
            connection.setReserved(false);
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

    public void closeConnections() throws SQLException {
        SQLException exception = null;
        for (DBNConnection connection : poolConnections) {
            try {
                connection.close();
            } catch (Exception e) {
                exception = new SQLException(e.getClass() + ": " + e.getMessage(), e);
            }
        }
        poolConnections.clear();

        if (mainConnection != null) {
            try {
                mainConnection.close();
            } catch (Exception e) {
                exception = new SQLException(e.getClass() + ": " + e.getMessage(), e);
            }
            mainConnection = null;
        }
        if (exception != null) {
            throw exception;
        }
    }

    @Deprecated
    public int getIdleMinutes() {
        return mainConnection == null ? 0 : mainConnection.getStatusMonitor().getIdleMinutes();
    }

    @Deprecated
    public void keepAlive(boolean check) {
        if (mainConnection != null) {
            mainConnection.getStatusMonitor().ping();
            if (check) mainConnection.isValid();
        }
    }

    public int getSize() {
        return poolConnections.size();
    }

    public int getPeakPoolSize() {
        return peakPoolSize;
    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
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
                if (connectionPool != null && TimeUtil.isOlderThan(connectionPool.lastAccessTimestamp, TimeUtil.FIVE_MINUTES)) {
                    // close connections only if pool is passive
                    for (DBNConnection connection : connectionPool.poolConnections) {
                        if (connection.isBusy()) return;
                    }

                    for (DBNConnection connection : connectionPool.poolConnections) {
                        ConnectionUtil.close(connection);
                    }
                    connectionPool.poolConnections.clear();
                }
            }

        }

        public void registerConnectionPool(ConnectionPool connectionPool) {
            connectionPools.add(new WeakReference<>(connectionPool));
        }
    }

    private static ConnectionPoolCleanTask POOL_CLEANER_TASK = new ConnectionPoolCleanTask();
    static {
        Timer poolCleaner = new Timer("DBN - Connection Pool Cleaner");
        poolCleaner.schedule(POOL_CLEANER_TASK, TimeUtil.ONE_MINUTE, TimeUtil.ONE_MINUTE);
    }
}
