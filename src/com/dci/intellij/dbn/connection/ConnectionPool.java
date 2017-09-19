package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;
import java.sql.Connection;
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
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
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

    private List<ConnectionWrapper> poolConnections = new CopyOnWriteArrayList<ConnectionWrapper>();
    private ConnectionWrapper mainConnection;

    ConnectionPool(@NotNull ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
        POOL_CLEANER_TASK.registerConnectionPool(this);
    }

    public DBNConnection createTestConnection() throws SQLException {
        return ConnectionUtil.connect(getConnectionHandler(), ConnectionType.TEST);
    }

    @Nullable
    public DBNConnection getMainConnection(boolean forceInit) throws SQLException {
        lastAccessTimestamp = System.currentTimeMillis();
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.setLastUsedConnection(connectionHandler);

        if (shouldInitMainConnection(forceInit)) {
            synchronized (this) {
                if (shouldInitMainConnection(forceInit)) {
                    try {
                        if (mainConnection != null) {
                            mainConnection.closeConnection();
                            mainConnection = null;
                        }

                        DBNConnection connection = ConnectionUtil.connect(connectionHandler, ConnectionType.MAIN);
                        mainConnection = new ConnectionWrapper(connection);
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

        return mainConnection == null ? null : mainConnection.getConnection();
    }

    boolean shouldInitMainConnection(boolean forceInit) throws SQLException {
        return forceInit && (mainConnection == null || mainConnection.isClosed() || !mainConnection.isValid());
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

        for (ConnectionWrapper connectionWrapper : poolConnections) {
            if (!connectionWrapper.isBusy()) {
                connectionWrapper.setBusy(true);
                if (connectionWrapper.isValid() && !connectionWrapper.isClosed()) {
                    connectionStatus.setConnected(true);
                    connectionStatus.setValid(true);
                    return connectionWrapper.getConnection();
                } else {
                    connectionWrapper.closeConnection();
                    poolConnections.remove(connectionWrapper);
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
        ConnectionWrapper connectionWrapper = new ConnectionWrapper(connection);
        connectionWrapper.setBusy(true);
        poolConnections.add(connectionWrapper);
        int size = poolConnections.size();
        if (size > peakPoolSize) peakPoolSize = size;
        lastAccessTimestamp = System.currentTimeMillis();
        LOGGER.debug("[DBN-INFO] Pool connection for '" + connectionName + "' created. Pool size = " + getSize());
        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            for (ConnectionWrapper connectionWrapper : poolConnections) {
                if (connectionWrapper.getConnection() == connection) {
                    ConnectionUtil.rollback(connection);
                    ConnectionUtil.setAutocommit(connection, true);
                    ConnectionUtil.setReadonly(connection, true);
                    connectionWrapper.setBusy(false);
                    break;
                }
            }
        }
        lastAccessTimestamp = System.currentTimeMillis();
    }

    public void dropConnection(Connection connection) {
        if (connection != null) {
            for (ConnectionWrapper connectionWrapper : poolConnections) {
                if (connectionWrapper.getConnection() == connection) {
                    poolConnections.remove(connectionWrapper);
                    connectionWrapper.closeConnection();
                    break;
                }
            }
        }
        lastAccessTimestamp = System.currentTimeMillis();
    }

    public void closeConnectionsSilently() {
        for (ConnectionWrapper connectionWrapper : poolConnections) {
            connectionWrapper.closeConnection();
        }
        poolConnections.clear();

        if (mainConnection != null) {
            mainConnection.closeConnection();
            mainConnection = null;
        }
    }

    public void closeConnections() throws SQLException {
        SQLException exception = null;
        for (ConnectionWrapper connectionWrapper : poolConnections) {
            try {
                connectionWrapper.getConnection().close();
            } catch (SQLException e) {
                exception = e;
            } catch (Exception e) {
                exception = new SQLException(e.getClass() + ": " + e.getMessage(), e);
            }
        }
        poolConnections.clear();

        if (mainConnection != null) {
            try {
                mainConnection.getConnection().close();
            } catch (SQLException e) {
                exception = e;
            } catch (Exception e) {
                exception = new SQLException(e.getClass() + ": " + e.getMessage(), e);
            }
            mainConnection = null;
        }
        if (exception != null) {
            throw exception;
        }
    }

    public int getIdleMinutes() {
        return mainConnection == null ? 0 : mainConnection.getIdleMinutes();
    }

    public void keepAlive(boolean check) {
        if (mainConnection != null) {
            if (check) mainConnection.isValid();
            mainConnection.keepAlive();
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

    boolean isMainConnection(Connection connection) {
        return mainConnection != null && mainConnection.getConnection() == connection;
    }

    boolean isPoolConnection(Connection connection) {
        for (ConnectionWrapper poolConnection : poolConnections) {
            if (poolConnection.getConnection() == connection) {
                return true;
            }
        }

        return false;
    }

    private static class ConnectionPoolCleanTask extends TimerTask {
        List<WeakReference<ConnectionPool>> connectionPools = new CopyOnWriteArrayList<WeakReference<ConnectionPool>>();

        public void run() {
            for (WeakReference<ConnectionPool> connectionPoolRef : connectionPools) {
                ConnectionPool connectionPool = connectionPoolRef.get();
                if (connectionPool != null && TimeUtil.isOlderThan(connectionPool.lastAccessTimestamp, TimeUtil.FIVE_MINUTES)) {
                    // close connections only if pool is passive
                    for (ConnectionWrapper connection : connectionPool.poolConnections) {
                        if (connection.isBusy()) return;
                    }

                    for (ConnectionWrapper connection : connectionPool.poolConnections) {
                        connection.closeConnection();
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


    private class ConnectionWrapper {
        private DBNConnection connection;
        private long lastCheckTimestamp;
        private long lastAccessTimestamp;
        private boolean isValid = true;
        private boolean isBusy = false;

        ConnectionWrapper(DBNConnection connection) {
            this.connection = connection;
            long currentTimeMillis = System.currentTimeMillis();
            lastCheckTimestamp = currentTimeMillis;
            lastAccessTimestamp = currentTimeMillis;
        }

        public boolean isValid() {
            long currentTimeMillis = System.currentTimeMillis();
            if (TimeUtil.isOlderThan(lastCheckTimestamp, TimeUtil.THIRTY_SECONDS)) {
                lastCheckTimestamp = currentTimeMillis;
                DatabaseMetadataInterface metadataInterface = getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                isValid = metadataInterface.isValid(connection);
            }
            return isValid;
        }

        int getIdleMinutes() {
            long idleTimeMillis = System.currentTimeMillis() - lastAccessTimestamp;
            return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
        }

        public DBNConnection getConnection() {
            lastAccessTimestamp = System.currentTimeMillis();
            return connection;
        }

        void closeConnection() {
            ConnectionUtil.closeConnection(connection);
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            ConnectionUtil.setAutoCommit(connection, autoCommit);
        }

        boolean isClosed() throws SQLException {
            return ConnectionUtil.isClosed(connection);
        }

        boolean isBusy() {
            return isBusy;
        }

        void setBusy(boolean isFree) {
            this.isBusy = isFree;
        }

        void keepAlive() {
            lastAccessTimestamp = System.currentTimeMillis();
        }
    }
}
