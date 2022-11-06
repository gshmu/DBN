package com.dci.intellij.dbn.connection.ssh;

import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSshTunnelSettings;

import java.util.HashMap;
import java.util.Map;

import static com.dci.intellij.dbn.common.component.Components.applicationService;

public class SshTunnelManager extends ApplicationComponentBase {
    private final Map<String, SshTunnelConnector> sshTunnelConnectors = new HashMap<>();

    public SshTunnelManager() {
        super("DBNavigator.SshTunnelManager");
    }

    public static SshTunnelManager getInstance() {
        return applicationService(SshTunnelManager.class);
    }

    public SshTunnelConnector ensureSshConnection(ConnectionSettings connectionSettings) throws Exception {
        ConnectionSshTunnelSettings sshSettings = connectionSettings.getSshTunnelSettings();
        if (sshSettings.isActive()) {
            ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
            DatabaseInfo databaseInfo = databaseSettings.getDatabaseInfo();
            String remoteHost = databaseInfo.getHost();
            int remotePort = Strings.parseInt(databaseInfo.getPort(), -1);

            String proxyHost = sshSettings.getHost();
            String proxyUser = sshSettings.getUser();
            int proxyPort = Strings.parseInt(sshSettings.getPort(), -1);
            String key = createKey(proxyHost, proxyPort, proxyUser, remoteHost, remotePort);
            SshTunnelConnector connector = sshTunnelConnectors.get(key);
            if (connector == null) {
                connector = new SshTunnelConnector(
                        proxyHost,
                        proxyPort,
                        proxyUser,
                        sshSettings.getAuthType(),
                        sshSettings.getKeyFile(),
                        sshSettings.getKeyPassphrase(),
                        sshSettings.getPassword(),
                        remoteHost,
                        remotePort);
                sshTunnelConnectors.put(key, connector);
            }
            if (!connector.isConnected()) {
                connector.createTunnel();
            }

            return connector;
        }
        return null;
    }

    private String createKey(String proxyHost, int proxyPort, String proxyUser, String remoteHost, int remotePort) {
        return remoteHost + ":" + remotePort + "@" + proxyHost + ":" + proxyPort + "/" + proxyUser;
    }
}
