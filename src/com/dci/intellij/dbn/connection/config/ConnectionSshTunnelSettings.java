package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSshTunnelSettingsForm;
import com.dci.intellij.dbn.connection.ssh.SshAuthType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

public class ConnectionSshTunnelSettings extends BasicProjectConfiguration<ConnectionSettings, ConnectionSshTunnelSettingsForm> {
    @Deprecated // TODO move to keychain
    private static final String OLD_PWD_ATTRIBUTE = "proxy-password";
    @Deprecated // TODO move to keychain
    private static final String TEMP_PWD_ATTRIBUTE = "deprecated-proxy-pwd";

    private boolean active = false;
    private String host;
    private String user;
    private String password;
    private String port = "22";
    private SshAuthType authType = SshAuthType.PASSWORD;
    private String keyFile;
    private String keyPassphrase;

    ConnectionSshTunnelSettings(ConnectionSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Connection SSH Tunnel Settings";
    }

    @Override
    public String getHelpTopic() {
        return "connectionSshTunnelSettings";
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

    public SshAuthType getAuthType() {
        return authType;
    }

    public void setAuthType(SshAuthType authType) {
        this.authType = authType;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getKeyPassphrase() {
        return keyPassphrase;
    }

    public void setKeyPassphrase(String keyPassphrase) {
        this.keyPassphrase = StringUtil.isEmpty(keyPassphrase) ? null : keyPassphrase;
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
        return "ssh-settings";
    }

    @Override
    public void readConfiguration(Element element) {
        active = getBoolean(element, "active", active);
        host = getString(element, "proxy-host", host);
        port = getString(element, "proxy-port", port);
        user = getString(element, "proxy-user", user);


        password = PasswordUtil.decodePassword(getString(element, TEMP_PWD_ATTRIBUTE, password));
        if (StringUtil.isEmpty(password)) {
            password = PasswordUtil.decodePassword(getString(element, OLD_PWD_ATTRIBUTE, password));
        }

        authType = getEnum(element, "auth-type", authType);
        keyFile = getString(element, "key-file", keyFile);
        keyPassphrase = PasswordUtil.decodePassword(getString(element, "key-passphrase", keyPassphrase));
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "active", active);
        setString(element, "proxy-host", host);
        setString(element, "proxy-port", port);
        setString(element, "proxy-user", user);
        setString(element, TEMP_PWD_ATTRIBUTE, PasswordUtil.encodePassword(password));
        setEnum(element, "auth-type", authType);
        setString(element, "key-file", keyFile);
        setString(element, "key-passphrase", PasswordUtil.encodePassword(keyPassphrase));
    }

    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }
}
