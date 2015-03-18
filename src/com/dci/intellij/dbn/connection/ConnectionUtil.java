package com.dci.intellij.dbn.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.intellij.openapi.diagnostic.Logger;

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
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();

        // do not retry connection on authentication error unless
        // credentials changed (account can be locked on several invalid trials)
        AuthenticationError authenticationError = connectionStatus.getAuthenticationError();
        String user = databaseSettings.getUser();
        String password = databaseSettings.getPassword();
        if (StringUtil.isEmpty(password)) {
            password = connectionHandler.getTemporaryPassword();
        }

        boolean osAuthentication = databaseSettings.isOsAuthentication();
        if (authenticationError != null && authenticationError.isSame(osAuthentication, user, password) && !authenticationError.isExpired()) {
            throw authenticationError.getException();
        }

        try {
            Connection connection = connect(databaseSettings, connectionHandler.getTemporaryPassword(), detailSettings.isEnableAutoCommit(), connectionStatus, connectionType);
            connectionStatus.setAuthenticationError(null);
            return connection;
        } catch (SQLException e) {
            if (connectionHandler.getDatabaseType() != DatabaseType.UNKNOWN) {
                DatabaseMessageParserInterface messageParserInterface = connectionHandler.getInterfaceProvider().getMessageParserInterface();
                if (messageParserInterface.isAuthenticationException(e)){
                    authenticationError = new AuthenticationError(osAuthentication, user, password, e);
                    connectionStatus.setAuthenticationError(authenticationError);
                }
            }
            throw e;
        }
    }

    public static Connection connect(ConnectionDatabaseSettings databaseSettings, String temporaryPassword, boolean autoCommit, @Nullable ConnectionStatus connectionStatus, ConnectionType connectionType) throws SQLException {
        try {
            Properties properties = new Properties();
            if (!databaseSettings.isOsAuthentication()) {
                String password = databaseSettings.getPassword();
                if (StringUtil.isEmpty(password)) {
                    password = temporaryPassword;
                }

                properties.put("user", databaseSettings.getUser());
                properties.put("password", password);
            }
            String appName = "Database Navigator - " + connectionType.getName() + "";
            properties.put("ApplicationName", appName);
            properties.put("v$session.program", appName);
            Map<String, String> configProperties = databaseSettings.getProperties();
            if (configProperties != null) {
                properties.putAll(configProperties);
            }

            Driver driver = DatabaseDriverManager.getInstance().getDriver(
                    databaseSettings.getDriverLibrary(),
                    databaseSettings.getDriver());

            Connection connection = driver.connect(databaseSettings.getDatabaseUrl(), properties);
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
            if (e instanceof SQLException)
                throw (SQLException) e;  else
                throw new SQLException(e.getMessage());
        }
    }

    private static DatabaseType getDatabaseType(String driver) {
        if (driver != null) {
            if (driver.toUpperCase().contains("ORACLE")) {
                return DatabaseType.ORACLE;
            } else if (driver.toUpperCase().contains("MYSQL")) {
                return DatabaseType.MYSQL;
            } else if (driver.toUpperCase().contains("POSTGRESQL")) {
                return DatabaseType.POSTGRES;
            }
        }
        return DatabaseType.UNKNOWN;

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
        if (productName.toUpperCase().contains("ORACLE")) {
            return DatabaseType.ORACLE;
        } else if (productName.toUpperCase().contains("MYSQL")) {
            return DatabaseType.MYSQL;
        } else if (productName.toUpperCase().contains("POSTGRESQL")) {
            return DatabaseType.POSTGRES;
        }
        return DatabaseType.UNKNOWN;
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
