package com.dci.intellij.dbn.editor.session.model;

import org.apache.commons.lang.StringUtils;

import com.dci.intellij.dbn.common.filter.Filter;

public class SessionBrowserModelFilter extends Filter<SessionBrowserModelRow>{
    private String user;
    private String host;
    private String status;

    @Override
    public boolean accepts(SessionBrowserModelRow row) {
        if (StringUtils.isNotEmpty(user) && !user.equals(row.getUser())) return false;
        if (StringUtils.isNotEmpty(host) && !host.equals(row.getHost())) return false;
        if (StringUtils.isNotEmpty(status) && !status.equals(row.getStatus())) return false;
        return true;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
