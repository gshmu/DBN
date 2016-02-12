package com.dci.intellij.dbn.database.common.util;

import java.sql.SQLException;

public class SkipEntrySQLException extends SQLException {
    public SkipEntrySQLException() {
        super("Skip entry");
    }
}
