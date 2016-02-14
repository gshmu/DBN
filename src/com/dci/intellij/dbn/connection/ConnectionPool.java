package com.dci.intellij.dbn.connection;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionPool implements Disposable {

    private static Logger LOGGER = LoggerFactory.createLogger();
    private long lastAccessTimestamp = 0;
    private int peakPoolSize = 0;
    private boolean isDisposed;

    protected final Logger log = Logger.getInstance(getClass().getName());
    private ConnectionHandlerRef connectionHandlerRef;

    private List<ConnectionWrapper> poolConnections = new CopyOnWriteArrayList<ConnectionWrapper>();
    private ConnectionWrapper standaloneConnection;

    public ConnectionPool(@NotNull ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
        POOL_CLEANER_TASK.registerConnectionPool(this);
    }

    public Connection getStandaloneConnection(boolean recover) throws SQLException {
        lastAccessTimestamp = System.currentTimeMillis();
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.setLastUsedConnection(connectionHandler);

        if (standaloneConnection != null && recover && (standaloneConnection.isClosed() || !standaloneConnection.isValid())) {
            standaloneConnection = null;
        }

        if (standaloneConnection == null) {
            synchronized (this) {
                if (standaloneConnection == null) {
                    try {
                        Connection connection = ConnectionUtil.connect(connectionHandler, ConnectionType.MAIN);
                        standaloneConnection = new ConnectionWrapper(connection);
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

        return standaloneConnection.getConnection();
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
    public Connection allocateConnection() throws SQLException {
        lastAccessTimestamp = System.currentTimeMillis();
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.setLastUsedConnection(connectionHandler);

        Connection connectionWrapper = lookupConnection();
        if (connectionWrapper == null)  {
            ConnectionDetailSettings detailSettings = connectionHandler.getSettings().getDetailSettings();
            if (poolConnections.size() >= detailSettings.getMaxConnectionPoolSize() && !ApplicationManager.getApplication().isDispatchThread()) {
                try {
                    Thread.sleep(TimeUtil.ONE_SECOND);
                    return allocateConnection();
                } catch (InterruptedException e) {
                    throw new SQLException("Could not allocate connection for '" + connectionHandler.getName() + "'. ");
                }
            }
            connectionWrapper = createConnection();
        }

        return connectionWrapper;
    }

    @Nullable
    private Connection lookupConnection() throws SQLException {
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
    private Connection createConnection() throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
        String connectionName = connectionHandler.getName();
        LOGGER.debug("[DBN-INFO] Attempt to create new pool connection for '" + connectionName + "'");
        Connection connection = ConnectionUtil.connect(connectionHandler, ConnectionType.POOL);
        ConnectionUtil.setAutoCommit(connection, true);
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

        if (standaloneConnection != null) {
            standaloneConnection.closeConnection();
            standaloneConnection = null;
        }
    }

    public void closeConnections() throws SQLException {
        SQLException exception = null;
        for (ConnectionWrapper connectionWrapper : poolConnections) {
            try {
                connectionWrapper.getConnection().close();
            } catch (SQLException e) {
                exception = e;
            }
        }
        poolConnections.clear();

        if (standaloneConnection != null) {
            try {
                standaloneConnection.getConnection().close();
            } catch (SQLException e) {
                exception = e;
            }
            standaloneConnection = null;
        }
        if (exception != null) {
            throw exception;
        }
    }

    public int getIdleMinutes() {
        return standaloneConnection == null ? 0 : standaloneConnection.getIdleMinutes();
    }

    public void keepAlive(boolean check) {
        if (standaloneConnection != null) {
            if (check) standaloneConnection.isValid();
            standaloneConnection.keepAlive();
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
        if (standaloneConnection != null && !standaloneConnection.isClosed()) {
            standaloneConnection.setAutoCommit(autoCommit);
        }
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
        private Connection connection;
        private long lastCheckTimestamp;
        private long lastAccessTimestamp;
        private boolean isValid = true;
        private boolean isBusy = false;

        public ConnectionWrapper(Connection connection) {
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
                return metadataInterface.isValid(connection);
            }
            return isValid;
        }

        public int getIdleMinutes() {
            long idleTimeMillis = System.currentTimeMillis() - lastAccessTimestamp;
            return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
        }

        public Connection getConnection() {
            lastAccessTimestamp = System.currentTimeMillis();
            return connection;
        }

        public void closeConnection() {
            ConnectionUtil.closeConnection(connection);
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            ConnectionUtil.setAutoCommit(connection, autoCommit);
        }

        public boolean isClosed() throws SQLException {
            return ConnectionUtil.isClosed(connection);
        }

        public boolean isBusy() {
            return isBusy;
        }

        public void setBusy(boolean isFree) {
            this.isBusy = isFree;
        }

        public void keepAlive() {
            lastAccessTimestamp = System.currentTimeMillis();
        }
    }
}
