package com.dci.intellij.dbn.common.database;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;

public class AuthenticationInfo implements com.dci.intellij.dbn.common.util.Cloneable<AuthenticationInfo>{
    private long timestamp = System.currentTimeMillis();
    private boolean osAuthentication;
    private boolean emptyPassword;
    private String user;
    private String password;

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

    public boolean isOsAuthentication() {
        return osAuthentication;
    }

    public void setOsAuthentication(boolean osAuthentication) {
        this.osAuthentication = osAuthentication;
    }

    public boolean isEmptyPassword() {
        return emptyPassword;
    }

    public void setEmptyPassword(boolean emptyPassword) {
        this.emptyPassword = emptyPassword;
    }

    public boolean isProvided() {
        return osAuthentication || (StringUtil.isNotEmpty(user) && (StringUtil.isNotEmpty(password) || emptyPassword));
    }

    public boolean isOlderThan(long millis) {
        return TimeUtil.isOlderThan(timestamp, millis);
    }

    public boolean isSame(AuthenticationInfo authenticationInfo) {
        return
            this.osAuthentication == authenticationInfo.osAuthentication &&
            CommonUtil.safeEqual(this.user, authenticationInfo.user) &&
            CommonUtil.safeEqual(this.password, authenticationInfo.password);
    }

    @Override
    public AuthenticationInfo clone() {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        authenticationInfo.user = user;
        authenticationInfo.password = password;
        authenticationInfo.osAuthentication = osAuthentication;
        authenticationInfo.emptyPassword = emptyPassword;
        return authenticationInfo;
    }
}
