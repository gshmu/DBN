package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.constant.PseudoConstant;
import com.dci.intellij.dbn.common.constant.PseudoConstantConverter;

import java.util.UUID;

public final class ConnectionId extends PseudoConstant<ConnectionId> {
    public static final ConnectionId VIRTUAL_ORACLE = get("virtual-oracle-connection");
    public static final ConnectionId VIRTUAL_MYSQL = get("virtual-mysql-connection");
    public static final ConnectionId VIRTUAL_POSTGRES = get("virtual-postgres-connection");
    public static final ConnectionId VIRTUAL_SQLITE = get("virtual-sqlite-connection");
    public static final ConnectionId VIRTUAL_ISO92_SQL = get("virtual-iso92-sql-connection");
    public static final ConnectionId UNKNOWN = get("unknown-connection");
    public static final ConnectionId DISPOSED = get("disposed-connection");

    private final int index;

    private ConnectionId(String id) {
        super(id);
        this.index = ConnectionIdIndex.next();
    }

    public int index() {
        return index;
    }

    public static ConnectionId get(String id) {
        return get(ConnectionId.class, id);
    }

    public static ConnectionId create() {
        return ConnectionId.get(UUID.randomUUID().toString());
    }

    public static class Converter extends PseudoConstantConverter<ConnectionId> {
        public Converter() {
            super(ConnectionId.class);
        }
    }

}
