package com.dci.intellij.dbn.editor.session;

import org.apache.commons.lang.StringUtils;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;

public class SessionBrowserFilterState extends Filter<SessionBrowserModelRow>{
    private String user;
    private String host;
    private String status;


    public SessionBrowserFilterState() {
    }

    private SessionBrowserFilterState(String user, String host, String status) {
        this.user = user;
        this.host = host;
        this.status = status;
    }

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

    public void clear() {
        user = null;
        host = null;
        status = null;
    }

    @Override
    protected SessionBrowserFilterState clone(){
        return new SessionBrowserFilterState(user, host, status);
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionBrowserFilterState that = (SessionBrowserFilterState) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        return !(status != null ? !status.equals(that.status) : that.status != null);

    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
