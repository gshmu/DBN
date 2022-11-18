package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.SchemaId;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class InterfaceContext {
    private final ConnectionRef connection;
    private final SchemaId schemaId;
    private final boolean readonly;

    private InterfaceContext(ConnectionHandler connection, SchemaId schemaId, boolean readonly) {
        this.connection = ConnectionRef.of(connection);
        this.schemaId = schemaId;
        this.readonly = readonly;
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    public static InterfaceContext create(ConnectionHandler connection, SchemaId schemaId, boolean readonly) {
        return new InterfaceContext(connection, schemaId, readonly);
    }
}
