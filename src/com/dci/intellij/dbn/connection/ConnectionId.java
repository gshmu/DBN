package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.constant.PseudoConstant;

public final class ConnectionId extends PseudoConstant<ConnectionId> {
    public static final ConnectionId VIRTUAL_ORACLE_CONNECTION = get("virtual-oracle-connection");
    public static final ConnectionId VIRTUAL_MYSQL_CONNECTION = get("virtual-mysql-connection");
    public static final ConnectionId VIRTUAL_POSTGRES_CONNECTION = get("virtual-postgres-connection");
    public static final ConnectionId VIRTUAL_SQLITE_CONNECTION = get("virtual-sqlite-connection");
    public static final ConnectionId VIRTUAL_ISO92_SQL_CONNECTION = get("virtual-iso92-sql-connection");
    public static final ConnectionId UNKNOWN_CONNECTION = get("unknown-connection");
    public static final ConnectionId DISPOSED_CONNECTION = get("disposed-connection");

    public ConnectionId(String id) {
        super(id);
    }

    public static ConnectionId get(String id) {
        return get(ConnectionId.class, id);
    }
}
