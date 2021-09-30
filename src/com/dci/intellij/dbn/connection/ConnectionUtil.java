package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.thread.Timeout;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;

public class ConnectionUtil {
    private ConnectionUtil() {}


    public static DBNConnection connect(ConnectionHandler connectionHandler, SessionId sessionId) throws SQLException {
        ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();
        ConnectionSettings connectionSettings = connectionHandler.getSettings();
        ConnectionPropertiesSettings propertiesSettings = connectionSettings.getPropertiesSettings();

        // do not retry connection on authentication error unless
        // credentials changed (account can be locked on several invalid trials)
        AuthenticationError authenticationError = connectionStatus.getAuthenticationError();
        AuthenticationInfo authenticationInfo = ensureAuthenticationInfo(connectionHandler);

        if (authenticationError != null && authenticationError.getAuthenticationInfo().isSame(authenticationInfo) && !authenticationError.isExpired()) {
            throw authenticationError.getException();
        }

        return DatabaseInterface.call(
                connectionHandler,
                (interfaceProvider) -> {
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
                            connectionStatus.setAuthenticationError(new AuthenticationError(authenticationInfo, e));
                        }
                        throw e;
                    }
                });
    }

    @NotNull
    private static AuthenticationInfo ensureAuthenticationInfo(ConnectionHandler connectionHandler) {
        ConnectionSettings connectionSettings = connectionHandler.getSettings();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo();
        if (!authenticationInfo.isProvided()) {
            authenticationInfo = connectionHandler.getTemporaryAuthenticationInfo();
        }
        return authenticationInfo;
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

        int connectTimeout = connectionSettings.getDetailSettings().getConnectivityTimeout();
        DBNConnection connection = Timeout.call(connectTimeout, null, true, () -> connector.connect());

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
        String driverClassName = databaseSettings.getDriver();
        if (driverSource == DriverSource.EXTERNAL) {
            driver = driverManager.getDriver(
                    new File(databaseSettings.getDriverLibrary()),
                    driverClassName);
        } else if (driverSource == DriverSource.BUILTIN) {
            DatabaseType databaseType = databaseSettings.getDatabaseType();
            boolean internal = databaseType == DatabaseType.ORACLE;
            driver = driverManager.getDriver(driverClassName, internal);

            if (driver == null) {
                File driverLibrary = driverManager.getInternalDriverLibrary(databaseType);
                if (driverLibrary != null) {
                    return driverManager.getDriver(driverLibrary, driverClassName);
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
}
