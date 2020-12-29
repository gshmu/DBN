package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSshTunnelSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSslSettings;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.ssh.SshTunnelConnector;
import com.dci.intellij.dbn.connection.ssh.SshTunnelManager;
import com.dci.intellij.dbn.connection.ssl.SslConnectionManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class Connector {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private final SessionId sessionId;
    private final AuthenticationInfo authenticationInfo;
    private final ConnectionSettings connectionSettings;
    private final ConnectionHandlerStatusHolder connectionStatus;
    private final DatabaseAttachmentHandler databaseAttachmentHandler;
    private final boolean autoCommit;

    Connector(
            SessionId sessionId,
            AuthenticationInfo authenticationInfo,
            ConnectionSettings connectionSettings,
            ConnectionHandlerStatusHolder connectionStatus,
            DatabaseAttachmentHandler databaseAttachmentHandler,
            boolean autoCommit) {
        this.sessionId = sessionId;
        this.authenticationInfo = authenticationInfo;
        this.connectionSettings = connectionSettings;
        this.connectionStatus = connectionStatus;
        this.databaseAttachmentHandler = databaseAttachmentHandler;
        this.autoCommit = autoCommit;
    }

    private SQLException exception;

    public SQLException getException() {
        return exception;
    }

    @Nullable
    public DBNConnection connect() {
        //trace(this);
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        try {
            Properties properties = new Properties();

            // AUTHENTICATION
            AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo();
            if (!authenticationInfo.isProvided() && this.authenticationInfo != null) {
                authenticationInfo = this.authenticationInfo;
            }

            AuthenticationType authenticationType = authenticationInfo.getType();
            if (authenticationType.isOneOf(AuthenticationType.USER, AuthenticationType.USER_PASSWORD)) {
                String user = authenticationInfo.getUser();
                if (StringUtil.isNotEmpty(user)) {
                    properties.put("user", user);
                }

                if (authenticationType == AuthenticationType.USER_PASSWORD) {
                    String password = authenticationInfo.getPassword();
                    if (StringUtil.isNotEmpty(password)) {
                        properties.put("password", password);
                    }
                }
            }

            // SESSION INFO
            ConnectionType connectionType = sessionId.getConnectionType();
            String appName = "Database Navigator - " + connectionType.getName();
            properties.put("ApplicationName", appName);

            DatabaseType databaseType = databaseSettings.getDatabaseType();
            if (databaseType == DatabaseType.GENERIC) {
                databaseType = DatabaseType.resolve(databaseSettings.getDriver());
            }

            if (databaseType == DatabaseType.ORACLE) {
                properties.put("v$session.program", appName);
            }

            Map<String, String> configProperties = databaseSettings.getParent().getPropertiesSettings().getProperties();
            if (configProperties != null) {
                properties.putAll(configProperties);
            }

            // DRIVER
            Driver driver = ResourceUtil.resolveDriver(databaseSettings);
            if (driver == null) {
                throw new SQLException("Could not resolve driver class.");
            }

            // SSL
            ConnectionSslSettings sslSettings = connectionSettings.getSslSettings();
            if (sslSettings.isActive()) {
                SslConnectionManager connectionManager = SslConnectionManager.getInstance();
                connectionManager.ensureSslConnection(connectionSettings);
                if (databaseType == DatabaseType.MYSQL) {
                    properties.setProperty("useSSL", "true");
                    properties.setProperty("requireSSL", "true");
                } else if (databaseType == DatabaseType.POSTGRES) {
                    properties.setProperty("ssl", "true");
                }
            }

            String connectionUrl = databaseSettings.getConnectionUrl();

            // SSH Tunnel
            ConnectionSshTunnelSettings sshTunnelSettings = connectionSettings.getSshTunnelSettings();
            if (sshTunnelSettings.isActive()) {
                SshTunnelManager sshTunnelManager = SshTunnelManager.getInstance();
                SshTunnelConnector sshTunnelConnector = sshTunnelManager.ensureSshConnection(connectionSettings);
                if (sshTunnelConnector != null) {
                    String localHost = sshTunnelConnector.getLocalHost();
                    String localPort = Integer.toString(sshTunnelConnector.getLocalPort());
                    connectionUrl = databaseSettings.getConnectionUrl(localHost, localPort);
                }
            }

            Connection connection = driver.connect(connectionUrl, properties);
            if (connection == null) {
                throw new SQLException("Driver failed to create connection for this configuration. No failure information provided by jdbc vendor.");
            }

            if (connectionStatus != null) {
                connectionStatus.setConnectionException(null);
                connectionStatus.setConnected(true);
                connectionStatus.setValid(true);
            }

            Project project = connectionSettings.getProject();
            if (databaseAttachmentHandler != null) {
                List<DatabaseFile> attachedDatabaseFiles = databaseSettings.getDatabaseInfo().getFiles().getSecondaryFiles();
                for (DatabaseFile databaseFile : attachedDatabaseFiles) {
                    String filePath = databaseFile.getPath();
                    try {
                        databaseAttachmentHandler.attachDatabase(connection, filePath, databaseFile.getSchema());
                    } catch (Exception e) {
                        NotificationSupport.sendErrorNotification(
                                project,
                                NotificationGroup.CONNECTION,
                                "Unable to attach database file {0}: {1}", filePath, e);
                    }
                }
            }

            DatabaseMetaData metaData = connection.getMetaData();
            databaseType = ResourceUtil.getDatabaseType(metaData);
            databaseSettings.setResolvedDatabaseType(databaseType);
            databaseSettings.setDatabaseVersion(ResourceUtil.getDatabaseVersion(metaData));
            databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);
            DBNConnection conn = DBNConnection.wrap(
                    project,
                    connection,
                    connectionSettings.getDatabaseSettings().getName(),
                    connectionType,
                    connectionSettings.getConnectionId(),
                    sessionId);

            ResourceUtil.setAutoCommit(conn, autoCommit);
            return conn;

        } catch (Throwable e) {
            DatabaseType databaseType = ResourceUtil.getDatabaseType(databaseSettings.getDriver());
            databaseSettings.setResolvedDatabaseType(databaseType);
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
