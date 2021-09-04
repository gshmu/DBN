package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SessionBrowserFilter implements Filter<SessionBrowserModelRow>, Cloneable<SessionBrowserFilter> {
    private String user;
    private String host;
    private String status;

    public SessionBrowserFilter() {
    }

    private SessionBrowserFilter(String user, String host, String status) {
        this.user = user;
        this.host = host;
        this.status = status;
    }

    @Override
    public boolean accepts(SessionBrowserModelRow row) {
        if (StringUtil.isNotEmpty(user) && !user.equals(row.getUser())) return false;
        if (StringUtil.isNotEmpty(host) && !host.equals(row.getHost())) return false;
        if (StringUtil.isNotEmpty(status) && !status.equals(row.getStatus())) return false;
        return true;
    }

    public void setFilterValue(SessionBrowserFilterType filterType, String value) {
        switch (filterType) {
            case USER: user = value; break;
            case HOST: host = value; break;
            case STATUS: status = value; break;
        }
    }

    public boolean isEmpty() {
        return StringUtil.isEmpty(getFilterValue(SessionBrowserFilterType.USER)) &&
                StringUtil.isEmpty(getFilterValue(SessionBrowserFilterType.HOST)) &&
                StringUtil.isEmpty(getFilterValue(SessionBrowserFilterType.STATUS));
    }

    public String getFilterValue(SessionBrowserFilterType filterType) {
        switch (filterType) {
            case USER: return user;
            case HOST: return host;
            case STATUS: return status;
            default: return null;
        }
    }

    public void clear() {
        user = null;
        host = null;
        status = null;
    }



    @Override
    public SessionBrowserFilter clone(){
        return new SessionBrowserFilter(user, host, status);
    }
}
