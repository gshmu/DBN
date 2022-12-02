package com.dci.intellij.dbn.common.exception;

import com.dci.intellij.dbn.common.util.Commons;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

public class Exceptions {
    public static final SQLNonTransientConnectionException DBN_NOT_CONNECTED_EXCEPTION = new SQLNonTransientConnectionException("Not connected to database");

    private Exceptions() {}

    @NotNull
    public static SQLException toSqlException(@NotNull Throwable e) {
        if (e instanceof SQLException) {
            return (SQLException) e;
        } else {
            return new SQLException(Commons.nvl(e.getMessage(), e.getClass().getSimpleName()), e);
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

    public static <T> T unsupported() {
        throw new UnsupportedOperationException();
    }
}
