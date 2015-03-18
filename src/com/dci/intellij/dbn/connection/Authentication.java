package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;

public class Authentication {
    private boolean osAuthentication;
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

    public boolean isProvided() {
        return osAuthentication || (StringUtil.isNotEmpty(user) && StringUtil.isNotEmpty(password));
    }

    public boolean isSame(Authentication authentication) {
        return
            this.osAuthentication == authentication.osAuthentication &&
            CommonUtil.safeEqual(this.user, authentication.user) &&
            CommonUtil.safeEqual(this.password, authentication.password);
    }

    @Override
    protected Authentication clone() {
        Authentication authentication = new Authentication();
        authentication.user = user;
        authentication.password = password;
        authentication.osAuthentication = osAuthentication;
        return authentication;
    }
}
