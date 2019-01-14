package com.dci.intellij.dbn.common.util;

import java.sql.SQLException;

public class ExceptionUtil {
    public static SQLException toSqlException(Throwable e) {
        if (e instanceof SQLException) {
            return (SQLException) e;
        } else {
            return new SQLException(e.getMessage(), e);
        }
    }

    public static RuntimeException toRuntimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e.getMessage(), e);
        }
    }
}
