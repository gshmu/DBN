package com.dci.intellij.dbn.connection.ssl;

import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSslSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class SslConnectionManager implements ApplicationComponent{
    private Map<String, SslConnection> sslConnectors = new THashMap<String, SslConnection>();

    public static SslConnectionManager getInstance() {
        return ApplicationManager.getApplication().getComponent(SslConnectionManager.class);
    }

    public SslConnection ensureSslConnection(ConnectionSettings connectionSettings) throws Exception {
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

    @Override
    public void initComponent() {
        EventUtil.subscribe(null, ProjectLifecycleListener.TOPIC, projectLifecycleListener);
    }

    @Override
    public void disposeComponent() { }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.SslConnectionManager";
    }

    /*********************************************************
     *              ProjectLifecycleListener                 *
     *********************************************************/
    private ProjectLifecycleListener projectLifecycleListener = new ProjectLifecycleListener.Adapter() {

        @Override
        public void projectComponentsInitialized(Project project) {
        }

        @Override
        public void afterProjectClosed(@NotNull Project project) {
        }
    };
}
