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
        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
        return StringUtil.isEmpty(password) ? credentialManager.getPassword(getConnectionId(), user) : password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        return !supported || osAuthentication || (StringUtil.isNotEmpty(user) && (StringUtil.isNotEmpty(getPassword()) || emptyPassword));
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
        this.password = PasswordUtil.decodePassword(getString(element, "password", null));

        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
        if (StringUtil.isEmpty(this.password)) {
            credentialManager.getPassword(getConnectionId(), user);
        } else {
            credentialManager.setPassword(getConnectionId(), user, password, temporary || isTransitory());
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
        setString(element, "deprecated-pwd", PasswordUtil.encodePassword(password));

        DatabaseCredentialManager.getInstance().setPassword(getConnectionId(), user, password, temporary || isTransitory());
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
}
