package com.dci.intellij.dbn.connection.ssl;

import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSslSettings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.dci.intellij.dbn.common.component.Components.applicationService;

public class SslConnectionManager extends ApplicationComponentBase {
    private final Map<String, SslConnection> sslConnectors = new HashMap<>();

    public SslConnectionManager() {
        super("DBNavigator.SslConnectionManager");
    }

    public static SslConnectionManager getInstance() {
        return applicationService(SslConnectionManager.class);
    }

    public SslConnection ensureSslConnection(ConnectionSettings connectionSettings) {
        ConnectionSslSettings sslSettings = connectionSettings.getSslSettings();
        if (sslSettings.isActive()) {
            String certificateAuthorityFilePath = sslSettings.getCertificateAuthorityFile();
            String clientCertificateFilePath = sslSettings.getClientCertificateFile();
            String clientKeyFilePath = sslSettings.getClientKeyFile();

            File certificateAuthorityFile = Strings.isEmpty(certificateAuthorityFilePath) ? null : new File(certificateAuthorityFilePath);
            File clientCertificateFile = Strings.isEmpty(clientCertificateFilePath) ? null : new File(clientCertificateFilePath);
            File clientKeyFile = Strings.isEmpty(clientKeyFilePath) ? null : new File(clientKeyFilePath);

            String key = createKey(certificateAuthorityFile, clientCertificateFile, clientKeyFile);
            SslConnection connector = sslConnectors.get(key);
            if (connector == null) {
                connector = new SslConnection(
                        certificateAuthorityFile,
                        clientCertificateFile,
                        clientKeyFile);
                sslConnectors.put(key, connector);
            }
            if (!connector.isConnected()) {
                connector.connect();
            }

            return connector;
        }
        return null;
    }

    private String createKey(File certificateAuthorityFile, File clientCertificateFile, File clientKeyFile) {
        return certificateAuthorityFile + "#" + clientCertificateFile + "#" + clientKeyFile;
    }
}
