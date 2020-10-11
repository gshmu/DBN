package com.dci.intellij.dbn.connection.ssh;

import com.dci.intellij.dbn.common.component.ApplicationComponent;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSshTunnelSettings;
import com.intellij.openapi.application.ApplicationManager;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SshTunnelManager implements ApplicationComponent {
    private final Map<String, SshTunnelConnector> sshTunnelConnectors = new THashMap<>();

    public static SshTunnelManager getInstance() {
        return ApplicationManager.getApplication().getComponent(SshTunnelManager.class);
    }

    public SshTunnelConnector ensureSshConnection(ConnectionSettings connectionSettings) throws Exception {
        ConnectionSshTunnelSettings sshSettings = connectionSettings.getSshTunnelSettings();
        if (sshSettings.isActive()) {
            ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
            DatabaseInfo databaseInfo = databaseSettings.getDatabaseInfo();
            String remoteHost = databaseInfo.getHost();
            int remotePort = StringUtil.parseInt(databaseInfo.getPort(), -1);

            String proxyHost = sshSettings.getHost();
            String proxyUser = sshSettings.getUser();
            int proxyPort = StringUtil.parseInt(sshSettings.getPort(), -1);
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

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.SshTunnelManager";
    }

}
