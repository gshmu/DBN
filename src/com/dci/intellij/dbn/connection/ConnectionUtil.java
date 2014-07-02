package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
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

public class ConnectionUtil {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static void closeResultSet(final ResultSet resultSet) {
        if (resultSet != null) {
            try {
                closeStatement(resultSet.getStatement());
                resultSet.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing result set", e);
            }
        }
    }

    public static void closeStatement(final Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Error closing statement", e);
        }

    }

    public static void closeConnection(final Connection connection) {
        if (connection != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        LOGGER.warn("Error closing connection", e);
                    }
                }
            }.start();
        }
    }

    public static Connection connect(ConnectionHandler connectionHandler) throws SQLException {
        ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
        ConnectionSettings connectionSettings = connectionHandler.getSettings();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        return connect(databaseSettings, detailSettings.getProperties(), detailSettings.isAutoCommit(), connectionStatus);
    }

    public static Connection connect(ConnectionDatabaseSettings databaseSettings, @Nullable Map<String, String> connectionProperties, boolean autoCommit, @Nullable ConnectionStatus connectionStatus) throws SQLException {
        try {
            Driver driver = DatabaseDriverManager.getInstance().getDriver(
                    databaseSettings.getDriverLibrary(),
                    databaseSettings.getDriver());

            Properties properties = new Properties();
            if (!databaseSettings.isOsAuthentication()) {
                properties.put("user", databaseSettings.getUser());
                properties.put("password", databaseSettings.getPassword());
            }
            if (connectionProperties != null) {
                properties.putAll(connectionProperties);
            }

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
            databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);

            return connection;
        } catch (Throwable e) {
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
            if (connection != null && !connection.getAutoCommit()) connection.rollback();
        } catch (SQLException e) {
            LOGGER.warn("Error rolling connection back", e);
        }
    }
    public static void setAutocommit(Connection connection, boolean autoCommit) {
        try {
            if (connection != null) connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            LOGGER.warn("Error setting autocommit to connection", e);
        }
    }


}
