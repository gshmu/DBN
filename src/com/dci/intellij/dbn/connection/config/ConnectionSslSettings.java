package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSslSettingsForm;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class ConnectionSslSettings extends Configuration<ConnectionSslSettingsForm> {
    private ConnectionSettings parent;

    private boolean active = false;
    private String certificateAuthorityFile;
    private String clientCertificateFile;
    private String clientKeyFile;

    public ConnectionSslSettings(ConnectionSettings parent) {
        this.parent = parent;
    }

    @Override
    public String getDisplayName() {
        return "Connection SSL Settings";
    }

    @Override
    public String getHelpTopic() {
        return "connectionSslSettings";
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCertificateAuthorityFile() {
        return certificateAuthorityFile;
    }

    public void setCertificateAuthorityFile(String certificateAuthorityFile) {
        this.certificateAuthorityFile = certificateAuthorityFile;
    }

    public String getClientCertificateFile() {
        return clientCertificateFile;
    }

    public void setClientCertificateFile(String clientCertificateFile) {
        this.clientCertificateFile = clientCertificateFile;
    }

    public String getClientKeyFile() {
        return clientKeyFile;
    }

    public void setClientKeyFile(String clientKeyFile) {
        this.clientKeyFile = clientKeyFile;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @NotNull
    @Override
    public ConnectionSslSettingsForm createConfigurationEditor() {
        return new ConnectionSslSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ssl-settings";
    }

    @Override
    public void readConfiguration(Element element) {
        active = getBoolean(element, "active", active);
        certificateAuthorityFile = getString(element, "certificate-authority-file", certificateAuthorityFile);
        clientCertificateFile = getString(element, "client-certificate-file", clientCertificateFile);
        clientKeyFile = getString(element, "client-key-file", clientKeyFile);
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "active", active);
        setString(element, "certificate-authority-file", certificateAuthorityFile);
        setString(element, "client-certificate-file", clientCertificateFile);
        setString(element, "client-key-file", clientKeyFile);
    }

    public Project getProject() {
        return parent.getProject();
    }

    public ConnectionId getConnectionId() {
        return parent.getConnectionId();
    }
}
