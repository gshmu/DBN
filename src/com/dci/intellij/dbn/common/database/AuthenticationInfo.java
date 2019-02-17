package com.dci.intellij.dbn.common.database;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.PasswordUtil;
import com.dci.intellij.dbn.credentials.DatabaseCredentialManager;
import org.jdom.Element;

public class AuthenticationInfo extends BasicConfiguration<ConnectionDatabaseSettings, ConfigurationEditorForm> implements Cloneable<AuthenticationInfo>{
    private long timestamp = System.currentTimeMillis();
    private boolean osAuthentication;
    private boolean emptyPassword;
    private boolean supported = true;
    private String user;
    private String password;
    private boolean temporary;

    public AuthenticationInfo(ConnectionDatabaseSettings parent, boolean temporary) {
        super(parent);
        this.temporary = temporary;
    }

    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtil.isEmpty(password) ? null : password;
    }

    public boolean isOsAuthentication() {
        return osAuthentication;
    }

    public void setOsAuthentication(boolean osAuthentication) {
        this.osAuthentication = osAuthentication;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public boolean isEmptyPassword() {
        return emptyPassword;
    }

    public void setEmptyPassword(boolean emptyPassword) {
        this.emptyPassword = emptyPassword;
    }

    public boolean isProvided() {
        return !supported || osAuthentication || (StringUtil.isNotEmpty(user) && (StringUtil.isNotEmpty(password) || emptyPassword));
    }

    public boolean isOlderThan(long millis) {
        return TimeUtil.isOlderThan(timestamp, millis);
    }

    public boolean isSame(AuthenticationInfo authenticationInfo) {
        return
            this.osAuthentication == authenticationInfo.osAuthentication &&
            CommonUtil.safeEqual(this.user, authenticationInfo.user) &&
            CommonUtil.safeEqual(this.getPassword(), authenticationInfo.getPassword());
    }

    @Override
    public void readConfiguration(Element element) {
        user = getString(element, "user", user);
        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();

        if (DatabaseCredentialManager.USE) {
            password = credentialManager.getPassword(getConnectionId(), user);
        }

        // old storage fallback - TODO cleanup
        if (StringUtil.isEmpty(password)) {
            password = PasswordUtil.decodePassword(getString(element, "password", null));
            if (StringUtil.isNotEmpty(password) && DatabaseCredentialManager.USE) {
                credentialManager.setPassword(getConnectionId(), user, password);
            }
        }

        emptyPassword = getBoolean(element, "empty-password", emptyPassword);
        osAuthentication = getBoolean(element, "os-authentication", osAuthentication);
        supported = getParent().getDatabaseType().isAuthenticationSupported();
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "os-authentication", osAuthentication);
        setBoolean(element, "empty-password", emptyPassword);
        setString(element, "user", nvl(user));

        String encodedPassword = PasswordUtil.encodePassword(password);
        if (DatabaseCredentialManager.USE)
            setString(element, "deprecated-pwd", encodedPassword); else
            setString(element, "password", encodedPassword);
    }

    @Override
    public AuthenticationInfo clone() {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo(getParent(), temporary);
        authenticationInfo.user = user;
        authenticationInfo.password = password;
        authenticationInfo.osAuthentication = osAuthentication;
        authenticationInfo.emptyPassword = emptyPassword;
        authenticationInfo.supported = supported;
        return authenticationInfo;
    }

    public void updateKeyChain(String oldUserName, String oldPassword) {
        if (supported && !temporary) {
            oldUserName = nvl(oldUserName);
            oldPassword = nvl(oldPassword);

            String newUserName = nvl(user);
            String newPassword = nvl(password);

            boolean userNameChanged = !CommonUtil.safeEqual(oldUserName, newUserName);
            boolean passwordChanged = !CommonUtil.safeEqual(oldPassword, newPassword);
            if (userNameChanged || passwordChanged) {
                DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
                ConnectionId connectionId = getConnectionId();

                if (userNameChanged) {
                    credentialManager.removePassword(connectionId, oldUserName);
                }
                if (StringUtil.isNotEmpty(newUserName) && StringUtil.isNotEmpty(newPassword)) {
                    credentialManager.setPassword(connectionId, newUserName, newPassword);
                }
            }
        }
    }
}
