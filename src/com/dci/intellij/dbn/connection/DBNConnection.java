package com.dci.intellij.dbn.connection;

import java.sql.Connection;

public class DBNConnection extends DBNConnectionBase {
    private ConnectionType type;
    public DBNConnection(Connection connection, ConnectionType type) {
        super(connection);
        this.type = type;
    }

    public ConnectionType getType() {
        return type;
    }

    public boolean isPoolConnection() {
        return type == ConnectionType.POOL;
    }

    public boolean isMainConnection() {
        return type == ConnectionType.MAIN;
    }

    public boolean iTestConnection() {
        return type == ConnectionType.TEST;
    }
}
