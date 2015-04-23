package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.ssh.SshTunnelConnector;
import com.dci.intellij.dbn.connection.ssh.SshTunnelManager;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ConnectionUtil {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static void closeResultSet(final ResultSet resultSet) {
        if (resultSet != null) {
            try {
                closeStatement(resultSet.getStatement());
                resultSet.close();
            } catch (Throwable e) {
                LOGGER.warn("Error closing result set: " + e.getMessage());
            }
        }
    }

    public static void closeStatement(final Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Throwable e) {
            LOGGER.warn("Error closing statement: " + e.getMessage());
        }

    }

    public static void closeConnection(final Connection connection) {
        if (connection != null) {
            new SimpleBackgroundTask("close connection") {
                @Override
                protected void execute() {
                    try {
                        connection.close();
                    } catch (Throwable e) {
                        LOGGER.warn("Error closing connection: " + e.getMessage());
                    }
                }
            }.start();
        }
    }

    public static Connection connect(ConnectionHandler connectionHandler, ConnectionType connectionType) throws SQLException {
        ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
        ConnectionSettings connectionSettings = connectionHandler.getSettings();
        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        ConnectionPropertiesSettings propertiesSettings = connectionSettings.getPropertiesSettings();

        // do not retry connection on authentication error unless
        // credentials changed (account can be locked on several invalid trials)
        AuthenticationError authenticationError = connectionStatus.getAuthenticationError();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        Authentication authentication = databaseSettings.getAuthentication();
        if (!authentication.isProvided()) {
            authentication = connectionHandler.getTemporaryAuthentication();
        }

        if (authenticationError != null && authenticationError.getAuthentication().isSame(authentication) && !authenticationError.isExpired()) {
            throw authenticationError.getException();
        }

        try {
            Connection connection = connect(connectionSettings, connectionHandler.getTemporaryAuthentication(), propertiesSettings.isEnableAutoCommit(), connectionStatus, connectionType);
            connectionStatus.setAuthenticationError(null);
            return connection;
        } catch (SQLException e) {
            if (connectionHandler.getDatabaseType() != DatabaseType.UNKNOWN) {
                DatabaseMessageParserInterface messageParserInterface = connectionHandler.getInterfaceProvider().getMessageParserInterface();
                if (messageParserInterface.isAuthenticationException(e)){
                    authenticationError = new AuthenticationError(authentication, e);
                    connectionStatus.setAuthenticationError(authenticationError);
                }
            }
            throw e;
        }
    }

    public static Connection connect(final ConnectionSettings connectionSettings, @Nullable Authentication temporaryAuthentication, final boolean autoCommit, @Nullable final ConnectionStatus connectionStatus, ConnectionType connectionType) throws SQLException {
        ConnectTimeoutCall connectCall = new ConnectTimeoutCall();
        connectCall.connectionType = connectionType;
        connectCall.temporaryAuthentication = temporaryAuthentication;
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
        private Authentication temporaryAuthentication;
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
                Authentication authentication = databaseSettings.getAuthentication();
                if (!authentication.isProvided() && temporaryAuthentication != null) {
                    authentication = temporaryAuthentication;
                }
                if (!authentication.isOsAuthentication()) {
                    String user = authentication.getUser();
                    String password = authentication.getPassword();
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

                final Driver driver = DatabaseDriverManager.getInstance().getDriver(
                        databaseSettings.getDriverLibrary(),
                        databaseSettings.getDriver());


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
                exception = new SQLException("SSH connection error: " + e.getMessage());
            }
            return null;
        }
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
        } catch (SQLException e) {
            LOGGER.warn("Error committing connection", e);
        }
    }

    public static void rollback(Connection connection) {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) connection.rollback();
        } catch (SQLException e) {
            LOGGER.warn("Error rolling connection back", e);
        }
    }
    public static void setAutocommit(Connection connection, boolean autoCommit) {
        try {
            if (connection != null && !connection.isClosed()) connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            LOGGER.warn("Error setting autocommit to connection", e);
        }
    }
}
