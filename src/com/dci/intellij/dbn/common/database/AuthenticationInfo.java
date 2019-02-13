package com.dci.intellij.dbn.common.database;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionIdProvider;
import com.dci.intellij.dbn.credentials.DatabaseCredentialManager;
import com.dci.intellij.dbn.language.common.WeakRef;

public class AuthenticationInfo implements com.dci.intellij.dbn.common.util.Cloneable<AuthenticationInfo>{
    private WeakRef<ConnectionIdProvider> parent;
    private long timestamp = System.currentTimeMillis();
    private boolean osAuthentication;
    private boolean emptyPassword;
    private boolean supported = true;
    private String user;
    private String password;
    private boolean temporary;

    public AuthenticationInfo(ConnectionIdProvider parent, boolean temporary) {
        this.parent = WeakRef.from(parent);
        this.temporary = temporary;
    }

    public ConnectionId getConnectionId() {
        return parent.getnn().getConnectionId();
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
        //DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
        //credentialManager.setPassword(getConnectionId(), user, password, temporary);
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
    public AuthenticationInfo clone() {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo(parent.getnn(), temporary);
        authenticationInfo.user = user;
        authenticationInfo.password = password;
        authenticationInfo.osAuthentication = osAuthentication;
        authenticationInfo.emptyPassword = emptyPassword;
        authenticationInfo.supported = supported;
        return authenticationInfo;
    }
}
