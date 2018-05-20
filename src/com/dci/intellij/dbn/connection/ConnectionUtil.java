package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import com.dci.intellij.dbn.connection.ssh.SshTunnelConnector;
import com.dci.intellij.dbn.connection.ssh.SshTunnelManager;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ConnectionUtil {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static boolean isClosed(final ResultSet resultSet) throws SQLException {
        try {
            return resultSet.isClosed();
        } catch (AbstractMethodError e) {
            // sqlite AbstractMethodError for osx
            return false;
        }
    }
    public static void cancel(final DBNStatement statement) {
        if (statement != null) {
            try {
                statement.cancel();
            } catch (Throwable e) {
                LOGGER.warn("Error cancelling statement: " + e.getMessage());
            } finally {
                close(statement);
            }
        }
    }

    public static <T extends AutoCloseable> T close(T resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Throwable e) {
                LOGGER.warn("Failed to close resource", e);
            }
        }
        return null;
    }

    public static void setAutoCommit(Connection connection, boolean autoCommit) throws SQLException {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            connection.setAutoCommit(autoCommit);
        }
    }

    public static DBNConnection connect(ConnectionHandler connectionHandler, ConnectionType connectionType) throws SQLException {
        ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();
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

        DatabaseType databaseType = connectionHandler.getDatabaseType();

        DatabaseInterfaceProvider interfaceProvider = databaseType == DatabaseType.UNKNOWN ? null : connectionHandler.getInterfaceProvider();
        try {
            DatabaseAttachmentHandler attachmentHandler = interfaceProvider == null ? null : interfaceProvider.getCompatibilityInterface().getDatabaseAttachmentHandler();
            DBNConnection connection = connect(
                    connectionSettings,
                    connectionType,
                    connectionStatus,
                    connectionHandler.getTemporaryAuthenticationInfo(),
                    propertiesSettings.isEnableAutoCommit(),
                    attachmentHandler);
            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
            connectionHandler.setConnectionInfo(connectionInfo);
            connectionStatus.setAuthenticationError(null);
            return connection;
        } catch (SQLException e) {
            if (interfaceProvider != null) {
                DatabaseMessageParserInterface messageParserInterface = interfaceProvider.getMessageParserInterface();
                if (messageParserInterface.isAuthenticationException(e)){
                    authenticationError = new AuthenticationError(authenticationInfo, e);
                    connectionStatus.setAuthenticationError(authenticationError);
                }
            }
            throw e;
        }
    }

    public static DBNConnection connect(
            ConnectionSettings connectionSettings,
            @NotNull ConnectionType connectionType,
            @Nullable ConnectionHandlerStatusHolder connectionStatus,
            @Nullable AuthenticationInfo temporaryAuthenticationInfo,
            boolean autoCommit,
            @Nullable DatabaseAttachmentHandler attachmentHandler) throws SQLException {
        ConnectTimeoutCall connectCall = new ConnectTimeoutCall();
        connectCall.connectionType = connectionType;
        connectCall.temporaryAuthenticationInfo = temporaryAuthenticationInfo;
        connectCall.connectionSettings = connectionSettings;
        connectCall.connectionStatus = connectionStatus;
        connectCall.autoCommit = autoCommit;
        connectCall.databaseAttachmentHandler = attachmentHandler;
        DBNConnection connection = connectCall.start();

        if (connectCall.exception != null) {
            throw connectCall.exception;
        }

        if (connection == null) {
            throw new SQLException("Could not connect to database. Communication timeout");
        }

        return connection;
    }

    private static class ConnectTimeoutCall extends SimpleTimeoutCall<DBNConnection> {
        private ConnectionType connectionType;
        private AuthenticationInfo temporaryAuthenticationInfo;
        private ConnectionSettings connectionSettings;
        private ConnectionHandlerStatusHolder connectionStatus;
        private DatabaseAttachmentHandler databaseAttachmentHandler;
        private boolean autoCommit;

        private SQLException exception;

        ConnectTimeoutCall() {
            super(30, TimeUnit.SECONDS, null, true);
        }

        @Override
        public DBNConnection call() {
            trace(this);
            ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
            try {
                final Properties properties = new Properties();
                AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo();
                if (!authenticationInfo.isProvided() && temporaryAuthenticationInfo != null) {
                    authenticationInfo = temporaryAuthenticationInfo;
                }
                if (authenticationInfo.isSupported() && !authenticationInfo.isOsAuthentication()) {
                    String user = authenticationInfo.getUser();
                    String password = authenticationInfo.getPassword();
                    properties.put("user", user);
                    if (StringUtil.isNotEmpty(password)) {
                        properties.put("password", password);
                    }
                }

                String appName = "Database Navigator - " + connectionType.getName();
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
                ConnectionUtil.setAutoCommit(connection, autoCommit);
                if (connectionStatus != null) {
                    connectionStatus.setConnectionException(null);
                    connectionStatus.setConnected(true);
                    connectionStatus.setValid(true);
                }

                if (databaseAttachmentHandler != null) {
                    List<DatabaseFile> attachedDatabaseFiles = databaseSettings.getDatabaseInfo().getFiles().getSecondaryFiles();
                    for (DatabaseFile databaseFile : attachedDatabaseFiles) {
                        String path = databaseFile.getPath();
                        try {
                            databaseAttachmentHandler.attachDatabase(connection, path, databaseFile.getSchema());
                        } catch (Exception e) {
                            NotificationUtil.sendErrorNotification(
                                    connectionSettings.getProject(),
                                    "Database Attachment Failure",
                                    "Unable to attach database file " + path + ". Cause: " + e.getMessage());
                        }
                    }
                }

                DatabaseType databaseType = getDatabaseType(connection);
                databaseSettings.setDatabaseType(databaseType);
                databaseSettings.setDatabaseVersion(getDatabaseVersion(connection));
                databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);
                return new DBNConnection(connection, connectionType, connectionSettings.getConnectionId());

            } catch (Throwable e) {
                DatabaseType databaseType = getDatabaseType(databaseSettings.getDriver());
                databaseSettings.setDatabaseType(databaseType);
                databaseSettings.setConnectivityStatus(ConnectivityStatus.INVALID);
                if (connectionStatus != null) {
                    connectionStatus.setConnectionException(e);
                    connectionStatus.setValid(false);
                }
                exception = e instanceof SQLException ? (SQLException) e : new SQLException("Connection error: " + e.getMessage(), e);
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

    public static void commit(DBNConnection connection) {
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
        } catch (SQLRecoverableException ignore){
        } catch (SQLException e) {
            LOGGER.warn("Error rolling back connection", e);
        }
    }

    public static void rollback(Connection connection, @Nullable Savepoint savepoint) {
        try {
            if (connection != null && savepoint != null && !connection.isClosed() && !connection.getAutoCommit()) connection.rollback(savepoint);
        } catch (SQLException ignore) {
        }
    }

    public static @Nullable Savepoint createSavepoint(Connection connection) {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
                return connection.setSavepoint();
            }
        } catch (SQLException ignore) {
        }
        return null;
    }

    public static void releaseSavepoint(Connection connection, @Nullable Savepoint savepoint) {
        try {
            if (connection != null && savepoint != null && !connection.isClosed() && !connection.getAutoCommit()) {
                connection.releaseSavepoint(savepoint);
            }
        } catch (SQLException ignore) {
        }
    }

    public static void setReadonly(DBNConnection connection, boolean readonly) {
        try {
            connection.setReadOnly(readonly);
        } catch (SQLException ignore) {
        }
    }

    public static void setAutocommit(DBNConnection connection, boolean autoCommit) {
        try {
            if (connection != null && !connection.isClosed()) {
                ConnectionUtil.setAutoCommit(connection, autoCommit);
            }
        } catch (SQLRecoverableException e){
            // ignore
        } catch (SQLException e) {
            LOGGER.warn("Error committing connection", e);
        }
    }
}
