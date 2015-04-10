package com.dci.intellij.dbn.database;

import java.sql.SQLException;

public interface DatabaseInterface {
    SQLException DBN_NOT_CONNECTED_EXCEPTION = new SQLException("Not connected to database");
    void reset();
}
