package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.util.TimeUtil;

import java.sql.SQLException;

public class AuthenticationError {
    public static final int THREE_MINUTES = TimeUtil.ONE_MINUTE * 3;
    private final AuthenticationInfo authenticationInfo;
    private final SQLException exception;
    private final long timestamp;

    public AuthenticationError(AuthenticationInfo authenticationInfo, SQLException exception) {
        this.authenticationInfo = authenticationInfo.clone();
        this.exception = exception;
        timestamp = System.currentTimeMillis();
    }

    public SQLException getException() {
        return exception;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public boolean isExpired() {
        return System.currentTimeMillis()- timestamp > THREE_MINUTES;
    }
}
