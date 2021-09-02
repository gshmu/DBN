package com.dci.intellij.dbn.connection.ssl;

import com.dci.intellij.dbn.common.component.ApplicationComponent;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSslSettings;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SslConnectionManager implements ApplicationComponent {
    private final Map<String, SslConnection> sslConnectors = new HashMap<>();

    public static SslConnectionManager getInstance() {
        return ApplicationManager.getApplication().getComponent(SslConnectionManager.class);
    }

    public SslConnection ensureSslConnection(ConnectionSettings connectionSettings) {
        ConnectionSslSettings sslSettings = connectionSettings.getSslSettings();
        if (sslSettings.isActive()) {
            String certificateAuthorityFilePath = sslSettings.getCertificateAuthorityFile();
            String clientCertificateFilePath = sslSettings.getClientCertificateFile();
            String clientKeyFilePath = sslSettings.getClientKeyFile();

            File certificateAuthorityFile = StringUtil.isEmpty(certificateAuthorityFilePath) ? null : new File(certificateAuthorityFilePath);
            File clientCertificateFile = StringUtil.isEmpty(clientCertificateFilePath) ? null : new File(clientCertificateFilePath);
            File clientKeyFile = StringUtil.isEmpty(clientKeyFilePath) ? null : new File(clientKeyFilePath);

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

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.SslConnectionManager";
    }
}
