package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
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
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class Connector {
    private SessionId sessionId;
    private AuthenticationInfo authenticationInfo;
    private ConnectionSettings connectionSettings;
    private ConnectionHandlerStatusHolder connectionStatus;
    private DatabaseAttachmentHandler databaseAttachmentHandler;
    private boolean autoCommit;

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
            if (authenticationInfo.isSupported() && !authenticationInfo.isOsAuthentication()) {
                String user = authenticationInfo.getUser();
                String password = authenticationInfo.getPassword();
                properties.put("user", user);
                if (StringUtil.isNotEmpty(password)) {
                    properties.put("password", password);
                }
            }

            // SESSION INFO
            ConnectionType connectionType = sessionId.getConnectionType();
            String appName = "Database Navigator - " + connectionType.getName();
            properties.put("ApplicationName", appName);
            properties.put("v$session.program", appName);
            Map<String, String> configProperties = databaseSettings.getParent().getPropertiesSettings().getProperties();
            if (configProperties != null) {
                properties.putAll(configProperties);
            }

            // DRIVER
            Driver driver = ConnectionUtil.resolveDriver(databaseSettings);
            if (driver == null) {
                throw new SQLException("Could not resolve driver class.");
            }

            // SSL
            ConnectionSslSettings sslSettings = connectionSettings.getSslSettings();
            if (sslSettings.isActive()) {
                SslConnectionManager connectionManager = SslConnectionManager.getInstance();
                connectionManager.ensureSslConnection(connectionSettings);
                DatabaseType databaseType = databaseSettings.getDatabaseType();
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
                throw new SQLException("Driver failed to create connection for this configuration. No failure information provided.");
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
                    String path = databaseFile.getPath();
                    try {
                        databaseAttachmentHandler.attachDatabase(connection, path, databaseFile.getSchema());
                    } catch (Exception e) {
                        NotificationUtil.sendErrorNotification(
                                project,
                                "Database Attachment Failure",
                                "Unable to attach database file " + path + ". Cause: " + e.getMessage());
                    }
                }
            }

            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                // need to try twice (don't remember why)
                connection.setAutoCommit(autoCommit);
            }

            DatabaseType databaseType = ConnectionUtil.getDatabaseType(connection);
            databaseSettings.setDatabaseType(databaseType);
            databaseSettings.setDatabaseVersion(ConnectionUtil.getDatabaseVersion(connection));
            databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);
            return new DBNConnection(
                    project,
                    connection,
                    connectionSettings.getDatabaseSettings().getName(),
                    connectionType,
                    connectionSettings.getConnectionId(),
                    sessionId);

        } catch (Throwable e) {
            DatabaseType databaseType = ConnectionUtil.getDatabaseType(databaseSettings.getDriver());
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
