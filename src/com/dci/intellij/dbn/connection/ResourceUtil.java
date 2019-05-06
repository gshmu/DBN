package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.Timeout;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Savepoint;

import static com.dci.intellij.dbn.DatabaseNavigator.DEBUG;

public class ResourceUtil {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static boolean isClosed(ResultSet resultSet) throws SQLException {
        try {
            return resultSet.isClosed();
        } catch (AbstractMethodError e) {
            // sqlite AbstractMethodError for osx
            return false;
        }
    }
    public static void cancel(DBNStatement statement) {
        if (statement != null) {
            try {
                long start = System.currentTimeMillis();
                if (DEBUG) LOGGER.debug("Cancelling statement (" + statement + ")");
                statement.cancel();
                if (DEBUG) LOGGER.debug("Done cancelling statement (" + statement + ") - " + (System.currentTimeMillis() - start) + "ms");
            } catch (SQLRecoverableException ignore) {
            } catch (Throwable e) {
                LOGGER.warn("Failed to cancel statement (" + statement + "): " + e.getMessage());
            } finally {
                close(statement);
            }
        }
    }

    public static <T extends AutoCloseable> void close(T resource) {
        if (resource != null) {
            try {
                long start = System.currentTimeMillis();
                if (DEBUG) LOGGER.debug("Closing resource (" + resource + ")");
                resource.close();
                if (DEBUG) LOGGER.debug("Done closing resource (" + resource + ") - " + (System.currentTimeMillis() - start) + "ms");
            } catch (SQLRecoverableException ignore) {
            } catch (Throwable e) {
                LOGGER.warn("Failed to close resource (" + resource + ")", e);
            }
        }
    }

    public static DBNConnection connect(ConnectionHandler connectionHandler, SessionId sessionId) throws SQLException {
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

        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        try {
            DatabaseAttachmentHandler attachmentHandler = interfaceProvider.getCompatibilityInterface().getDatabaseAttachmentHandler();
            DBNConnection connection = connect(
                    connectionSettings,
                    connectionStatus,
                    connectionHandler.getTemporaryAuthenticationInfo(),
                    sessionId,
                    propertiesSettings.isEnableAutoCommit(),
                    attachmentHandler);
            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
            connectionHandler.setConnectionInfo(connectionInfo);
            connectionStatus.setAuthenticationError(null);
            connectionHandler.getCompatibility().read(connection.getMetaData());
            return connection;
        } catch (SQLException e) {
            DatabaseMessageParserInterface messageParserInterface = interfaceProvider.getMessageParserInterface();
            if (messageParserInterface.isAuthenticationException(e)){
                authenticationInfo.setPassword(null);
                authenticationError = new AuthenticationError(authenticationInfo, e);
                connectionStatus.setAuthenticationError(authenticationError);
            }
            throw e;
        }
    }

    @NotNull
    public static DBNConnection connect(
            ConnectionSettings connectionSettings,
            @Nullable ConnectionHandlerStatusHolder connectionStatus,
            @Nullable AuthenticationInfo temporaryAuthenticationInfo,
            @NotNull SessionId sessionId,
            boolean autoCommit,
            @Nullable DatabaseAttachmentHandler attachmentHandler) throws SQLException {
        Connector connector = new Connector(
                sessionId,
                temporaryAuthenticationInfo,
                connectionSettings,
                connectionStatus,
                attachmentHandler,
                autoCommit);

        DBNConnection connection = Timeout.call(30, null, true, () -> connector.connect());

        SQLException exception = connector.getException();
        if (exception != null) {
            throw exception;
        }

        if (connection == null) {
            throw new SQLException("Could not connect to database. Communication timeout");
        }

        return connection;
    }

    @Nullable
    static Driver resolveDriver(ConnectionDatabaseSettings databaseSettings) throws Exception {
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

    protected static DatabaseType getDatabaseType(String driver) {
        return DatabaseType.resolve(driver);

    }

    public static double getDatabaseVersion(DatabaseMetaData databaseMetaData) throws SQLException {
        int majorVersion = databaseMetaData.getDatabaseMajorVersion();
        int minorVersion = databaseMetaData.getDatabaseMinorVersion();
        return new Double(majorVersion + "." + minorVersion);
    }

    public static DatabaseType getDatabaseType(DatabaseMetaData databaseMetaData) throws SQLException {
        String productName = databaseMetaData.getDatabaseProductName();
        return DatabaseType.resolve(productName);
    }

    public static void commitSilently(DBNConnection connection) {
        try {
            commit(connection);
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            LOGGER.warn("Commit failed", e);
        }
    }

    public static void commit(DBNConnection connection) throws SQLException {
        try {
            if (connection != null) connection.commit();
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    "Commit",
                    "Failed to commit",
                    connection,
                    e);
            throw e;
        }
    }

    public static void rollbackSilently(DBNConnection connection) {
        try {
            rollback(connection);
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            LOGGER.warn("Rollback failed", e);
        }

    }
    public static void rollback(DBNConnection connection) throws SQLException {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) connection.rollback();
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    "Rollback",
                    "Failed to rollback",
                    connection,
                    e);
            throw e;
        }
    }

    public static void rollbackSilently(DBNConnection connection, @Nullable Savepoint savepoint) {
        try {
            rollback(connection, savepoint);
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            LOGGER.warn("Savepoint rollback failed", e);
        }
    }

    public static void rollback(DBNConnection connection, @Nullable Savepoint savepoint) throws SQLException {
        try {
            if (connection != null && savepoint != null && !connection.isClosed() && !connection.getAutoCommit()) connection.rollback(savepoint);
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    "Savepoint",
                    "Failed to rollback savepoint for",
                    connection,
                    e);
            throw e;
        }
    }

    public static @Nullable Savepoint createSavepoint(DBNConnection connection) {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
                return connection.setSavepoint();
            }
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    "Savepoint",
                    "Failed to create savepoint for",
                    connection,
                    e);
        }
        return null;
    }

    public static void releaseSavepoint(DBNConnection connection, @Nullable Savepoint savepoint) {
        try {
            if (connection != null && savepoint != null && !connection.isClosed() && !connection.getAutoCommit()) {
                connection.releaseSavepoint(savepoint);
            }
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    "Savepoint",
                    "Failed to release savepoint for",
                    connection,
                    e);
        }
    }

    public static void setReadonly(ConnectionHandler connectionHandler, DBNConnection connection, boolean readonly) {
        boolean readonlySupported = DatabaseFeature.READONLY_CONNECTIVITY.isSupported(connectionHandler);
        if (readonlySupported) {
            try {
                connection.setReadOnly(readonly);
            } catch (SQLException e) {
            sentWarningNotification(
                    "Readonly",
                    "Failed to initialize readonly status for",
                    connection,
                    e);
            }
        }
    }

    public static void setAutoCommit(DBNConnection connection, boolean autoCommit) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLRecoverableException ignore) {
        } catch (Exception e) {
            sentWarningNotification(
                    "Auto-Commit",
                    "Failed to change auto-commit status for",
                    connection,
                    e);
        }
    }

    private static void sentWarningNotification(String title, String message, DBNConnection connection, Exception e) {
        String name = connection.getName();
        SessionId sessionId = connection.getSessionId();
        String errorMessage = e.getMessage();
        String notificationMessage = message + " connection \"" + name + " (" + sessionId + ")\": " + errorMessage;

        NotificationUtil.sendWarningNotification(
                connection.getProject(),
                Constants.DBN_TITLE_PREFIX + title,
                notificationMessage);

    }
}
