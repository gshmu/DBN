package com.dci.intellij.dbn.connection;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.CommonUtil;

public class AuthenticationError {
    private boolean osAuthentication;
    private String user;
    private String password;
    private SQLException exception;

    public AuthenticationError(boolean osAuthentication, String user, String password, SQLException exception) {
        this.osAuthentication = osAuthentication;
        this.user = user;
        this.password = password;
        this.exception = exception;
    }

    public SQLException getException() {
        return exception;
    }

    public boolean isSame(boolean osAuthentication, String user, String password) {
        return
            this.osAuthentication == osAuthentication &&
            CommonUtil.safeEqual(this.user, user) &&
            CommonUtil.safeEqual(this.password, password);
    }
}
