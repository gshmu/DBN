package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class DatabaseInterfaceContext {
    private final ConnectionId connectionId;
    private final SchemaId schemaId;
    private final boolean readonly;

    private DatabaseInterfaceContext(ConnectionId connectionId, SchemaId schemaId, boolean readonly) {
        this.connectionId = connectionId;
        this.schemaId = schemaId;
        this.readonly = readonly;
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return ConnectionHandler.ensure(connectionId);
    }

    public static DatabaseInterfaceContext create(ConnectionId connectionId, SchemaId schemaId, boolean readonly) {
        return new DatabaseInterfaceContext(connectionId, schemaId, readonly);
    }
}
