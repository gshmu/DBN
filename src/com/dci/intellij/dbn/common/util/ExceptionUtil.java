package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class ExceptionUtil {
    @NotNull
    public static SQLException toSqlException(@NotNull Throwable e) {
        if (e instanceof SQLException) {
            return (SQLException) e;
        } else {
            return new SQLException(e.getMessage(), e);
        }
    }

    @NotNull
    public static SQLException toSqlException(@NotNull Throwable e, String s) {
        return e instanceof SQLException ?
                (SQLException) e :
                new SQLException(s + ": [" + e.getClass().getSimpleName() + "] " + e.getMessage(), e);
    }

    @NotNull
    public static RuntimeException toRuntimeException(@NotNull Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e.getMessage(), e);
        }
    }
}
