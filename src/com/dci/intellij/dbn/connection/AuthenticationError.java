package com.dci.intellij.dbn.connection;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.TimeUtil;

public class AuthenticationError {
    public static final int THREE_MINUTES = TimeUtil.ONE_MINUTE * 3;
    private Authentication authentication;
    private SQLException exception;
    private long timestamp;

    public AuthenticationError(Authentication authentication, SQLException exception) {
        this.authentication = authentication.clone();
        this.exception = exception;
        timestamp = System.currentTimeMillis();
    }

    public SQLException getException() {
        return exception;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public boolean isExpired() {
        return System.currentTimeMillis()- timestamp > THREE_MINUTES;
    }
}
