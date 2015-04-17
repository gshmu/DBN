package com.dci.intellij.dbn.connection.config;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSshTunnelSettingsForm;
import com.intellij.openapi.project.Project;

public class ConnectionSshTunnelSettings extends Configuration<ConnectionSshTunnelSettingsForm> {
    private ConnectionSettings parent;

    private boolean enabled = false;
    private String host;
    private String user;
    private String password;
    private int port = 22;

    public ConnectionSshTunnelSettings(ConnectionSettings parent) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @NotNull
    @Override
    public ConnectionSshTunnelSettingsForm createConfigurationEditor() {
        return new ConnectionSshTunnelSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ssh-tunnel";
    }

    @Override
    public void readConfiguration(Element element) {
        enabled = getBoolean(element, "enabled", enabled);
        host = getString(element, "host", host);
        port = getInteger(element, "port", port);
        user = getString(element, "user", user);
        password = PasswordUtil.decodePassword(getString(element, "password", password));
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "enabled", enabled);
        setString(element, "host", host);
        setInteger(element, "port", port);
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
