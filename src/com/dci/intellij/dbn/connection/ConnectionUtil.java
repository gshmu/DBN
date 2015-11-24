package com.dci.intellij.dbn.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.ssh.SshTunnelConnector;
import com.dci.intellij.dbn.connection.ssh.SshTunnelManager;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

public class ConnectionUtil {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static void closeResultSet(final ResultSet resultSet) {
        if (resultSet != null) {
            if (ApplicationManager.getApplication().isDispatchThread()) {
                new SimpleBackgroundTask("close result set") {
                    @Override
                    protected void execute() {
                        closeResultSet(resultSet);
                    }
                }.start();
            } else {
                try {
                    closeStatement(resultSet.getStatement());
                    resultSet.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static void closeStatement(final Statement statement) {
        if (statement != null) {
            if (ApplicationManager.getApplication().isDispatchThread()) {
                new SimpleBackgroundTask("close statement") {
                    @Override
                    protected void execute() {
                        closeStatement(statement);
                    }
                }.start();
            } else {
                try {
                    statement.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static void cancelStatement(final Statement statement) {
        if (statement != null) {
            try {
                statement.cancel();
            } catch (Throwable e) {
                LOGGER.warn("Error cancelling statement: " + e.getMessage());
            } finally {
                closeStatement(statement);
            }
        }
    }

    public static void closeConnection(final Connection connection) {
        if (connection != null) {
            if (ApplicationManager.getApplication().isDispatchThread()) {
                new SimpleBackgroundTask("close connection") {
                    @Override
                    protected void execute() {
                        closeConnection(connection);
                    }
                }.start();
            } else {
                try {
                    connection.close();
                } catch (Throwable e) {
                    LOGGER.warn("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public static Connection connect(ConnectionHandler connectionHandler, ConnectionType connectionType) throws SQLException {
        ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
        ConnectionSettings connectionSettings = connectionHandler.getSettings();
        ConnectionPropertiesSettings propertiesSettings = connectionSettings.getPropertiesSettings();

        // do not retry connection on authentication error unless
        // credentials changed (account can be locked on several invalid trials)
        AuthenticationError authenticationError = connectionStatus.getAuthenticationError();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo();
        if (!authenticationInfo.isProvided()) {
            authenticationInfo = connectionHandler.getTemporaryAuthenticationInfo();
        }

        if (authenticationError != null && authenticationError.getAuthenticationInfo().isSame(authenticationInfo) && !authenticationError.isExpired()) {
            throw authenticationError.getException();
        }

        try {
            Connection connection = connect(connectionSettings, connectionHandler.getTemporaryAuthenticationInfo(), propertiesSettings.isEnableAutoCommit(), connectionStatus, connectionType);
            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
            connectionHandler.setConnectionInfo(connectionInfo);
            connectionStatus.setAuthenticationError(null);
            return connection;
        } catch (SQLException e) {
            if (connectionHandler.getDatabaseType() != DatabaseType.UNKNOWN) {
                DatabaseMessageParserInterface messageParserInterface = connectionHandler.getInterfaceProvider().getMessageParserInterface();
                if (messageParserInterface.isAuthenticationException(e)){
                    authenticationError = new AuthenticationError(authenticationInfo, e);
                    connectionStatus.setAuthenticationError(authenticationError);
                }
            }
            throw e;
        }
    }

    public static Connection connect(final ConnectionSettings connectionSettings, @Nullable AuthenticationInfo temporaryAuthenticationInfo, final boolean autoCommit, @Nullable final ConnectionStatus connectionStatus, ConnectionType connectionType) throws SQLException {
        ConnectTimeoutCall connectCall = new ConnectTimeoutCall();
        connectCall.connectionType = connectionType;
        connectCall.temporaryAuthenticationInfo = temporaryAuthenticationInfo;
        connectCall.connectionSettings = connectionSettings;
        connectCall.connectionStatus = connectionStatus;
        connectCall.autoCommit = autoCommit;
        Connection connection = connectCall.start();

        if (connectCall.exception != null) {
            throw connectCall.exception;
        }

        if (connection == null) {
            throw new SQLException("Could not connect to database. Communication timeout");
        }

        return connection;
    }

    private static class ConnectTimeoutCall extends SimpleTimeoutCall<Connection> {
        private ConnectionType connectionType;
        private AuthenticationInfo temporaryAuthenticationInfo;
        private ConnectionSettings connectionSettings;
        private ConnectionStatus connectionStatus;
        private boolean autoCommit;

        private SQLException exception;

        public ConnectTimeoutCall() {
            super(30, TimeUnit.SECONDS, null);
        }

        @Override
        public Connection call() throws Exception{
            ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
            try {
                final Properties properties = new Properties();
                AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo();
                if (!authenticationInfo.isProvided() && temporaryAuthenticationInfo != null) {
                    authenticationInfo = temporaryAuthenticationInfo;
                }
                if (!authenticationInfo.isOsAuthentication()) {
                    String user = authenticationInfo.getUser();
                    String password = authenticationInfo.getPassword();
                    properties.put("user", user);
                    if (StringUtil.isNotEmpty(password)) {
                        properties.put("password", password);
                    }
                }

                String appName = "Database Navigator - " + connectionType.getName() + "";
                properties.put("ApplicationName", appName);
                properties.put("v$session.program", appName);
                Map<String, String> configProperties = databaseSettings.getParent().getPropertiesSettings().getProperties();
                if (configProperties != null) {
                    properties.putAll(configProperties);
                }

                Driver driver = resolveDriver(databaseSettings);
                if (driver == null) {
                    throw new SQLException("Could not resolve driver class.");
                }

                String connectionUrl = databaseSettings.getConnectionUrl();

                SshTunnelManager sshTunnelManager = SshTunnelManager.getInstance();
                SshTunnelConnector sshTunnelConnector = sshTunnelManager.ensureSshConnection(databaseSettings.getParent());
                if (sshTunnelConnector != null) {
                    String localHost = sshTunnelConnector.getLocalHost();
                    String localPort = Integer.toString(sshTunnelConnector.getLocalPort());
                    connectionUrl = databaseSettings.getConnectionUrl(localHost, localPort);
                }

                Connection connection = driver.connect(connectionUrl, properties);
                if (connection == null) {
                    throw new SQLException("Driver refused to create connection for this configuration. No failure information provided.");
                }
                connection.setAutoCommit(autoCommit);
                if (connectionStatus != null) {
                    connectionStatus.setStatusMessage(null);
                    connectionStatus.setConnected(true);
                    connectionStatus.setValid(true);
                }

                DatabaseType databaseType = getDatabaseType(connection);
                databaseSettings.setDatabaseType(databaseType);
                databaseSettings.setDatabaseVersion(getDatabaseVersion(connection));
                databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);
                return connection;

            } catch (Throwable e) {
                DatabaseType databaseType = getDatabaseType(databaseSettings.getDriver());
                databaseSettings.setDatabaseType(databaseType);
                databaseSettings.setConnectivityStatus(ConnectivityStatus.INVALID);
                if (connectionStatus != null) {
                    connectionStatus.setStatusMessage(e.getMessage());
                    connectionStatus.setConnected(false);
                    connectionStatus.setValid(false);
                }
                exception = new SQLException("Connection error: " + e.getMessage());
            }
            return null;
        }
    }

    @Nullable
    private static Driver resolveDriver(ConnectionDatabaseSettings databaseSettings) throws Exception {
        Driver driver = null;
        DatabaseDriverManager driverManager = DatabaseDriverManager.getInstance();
        DriverSource driverSource = databaseSettings.getDriverSource();
        if (driverSource == DriverSource.EXTERNAL) {
            driver = driverManager.getDriver(
                    databaseSettings.getDriverLibrary(),
                    databaseSettings.getDriver());
        } else if (driverSource == DriverSource.BUILTIN) {
            driver = driverManager.getDriver(databaseSettings.getDriver());
            if (driver == null) {
                String driverLibrary = driverManager.getInternalDriverLibrary(databaseSettings.getDatabaseType());
                if (driverLibrary != null) {
                    return driverManager.getDriver(driverLibrary, databaseSettings.getDriver());
                }
            }
        }

        return driver;
    }

    private static DatabaseType getDatabaseType(String driver) {
        return DatabaseType.resolve(driver);

    }

    public static double getDatabaseVersion(Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        int majorVersion = databaseMetaData.getDatabaseMajorVersion();
        int minorVersion = databaseMetaData.getDatabaseMinorVersion();
        return new Double(majorVersion + "." + minorVersion);
    }

    public static DatabaseType getDatabaseType(Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        String productName = databaseMetaData.getDatabaseProductName();
        return DatabaseType.resolve(productName);
    }

    public static void commit(Connection connection) {
        try {
            if (connection != null) connection.commit();
        } catch (SQLRecoverableException e){
            // ignore
        } catch (SQLException e) {
            LOGGER.warn("Error committing connection", e);
        }
    }

    public static void rollback(Connection connection) {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) connection.rollback();
        } catch (SQLRecoverableException e){
            // ignore
        } catch (SQLException e) {
            LOGGER.warn("Error committing connection", e);
        }
    }
    public static void setAutocommit(Connection connection, boolean autoCommit) {
        try {
            if (connection != null && !connection.isClosed()) connection.setAutoCommit(autoCommit);
        } catch (SQLRecoverableException e){
            // ignore
        } catch (SQLException e) {
            LOGGER.warn("Error committing connection", e);
        }
    }


    public static boolean isClosed(final Connection connection) {
        return new SimpleTimeoutCall<Boolean>(2, TimeUnit.SECONDS, false) {
            @Override
            public Boolean call() throws Exception {
                return connection.isClosed();
            }
        }.start();
    }
}
