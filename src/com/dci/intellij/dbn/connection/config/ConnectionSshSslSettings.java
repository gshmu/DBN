package com.dci.intellij.dbn.connection.config;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSshSslSettingsForm;
import com.intellij.openapi.project.Project;

public class ConnectionSshSslSettings extends Configuration<ConnectionSshSslSettingsForm> {
    private ConnectionSettings parent;

    private boolean active = false;
    private String host;
    private String user;
    private String password;
    private String port = "22";

    public ConnectionSshSslSettings(ConnectionSettings parent) {
        this.parent = parent;
    }

    public String getDisplayName() {
        return "Connection SSH Tunnel Settings";
    }

    public String getHelpTopic() {
        return "connectionPropertySettings";
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @NotNull
    @Override
    public ConnectionSshSslSettingsForm createConfigurationEditor() {
        return new ConnectionSshSslSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ssh-tunnel";
    }

    @Override
    public void readConfiguration(Element element) {
        active = getBoolean(element, "active", active);
        host = getString(element, "host", host);
        port = getString(element, "port", port);
        user = getString(element, "user", user);
        password = PasswordUtil.decodePassword(getString(element, "password", password));
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "active", active);
        setString(element, "host", host);
        setString(element, "port", port);
        setString(element, "user", user);
        setString(element, "password", PasswordUtil.encodePassword(password));

    }

    public Project getProject() {
        return parent.getProject();
    }

    public String getConnectionId() {
        return parent.getConnectionId();
    }
}
