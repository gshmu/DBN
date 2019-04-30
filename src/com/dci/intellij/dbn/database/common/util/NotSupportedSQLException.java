package com.dci.intellij.dbn.database.common.util;

import java.sql.SQLException;

public class NotSupportedSQLException extends SQLException {
    public NotSupportedSQLException(String message) {
        super(message);
    }
}
