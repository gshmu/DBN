package com.dci.intellij.dbn.connection.context;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceContext;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaces;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DatabaseContextBase extends DatabaseContext{

    @Nullable
    default ConnectionId getConnectionId() {
        ConnectionHandler connection = getConnection();
        return connection == null ? null : connection.getConnectionId();
    }

    default SessionId getSessionId() {
        return null;
    }

    @Nullable
    default DatabaseSession getSession() {
        return null;
    }

    @Nullable
    default SchemaId getSchemaId() {
        return null;
    }

    @Nullable
    default DBSchema getSchema() {
        SchemaId schemaId = getSchemaId();
        if (schemaId == null) return null;

        ConnectionHandler connection = getConnection();
        if (connection == null) return null;

        return connection.getSchema(schemaId);
    }

    @Nullable
    @Override
    default String getSchemaName() {
        SchemaId schemaId = getSchemaId();
        return schemaId == null ? null : schemaId.getName();
    }

    @Nullable
    ConnectionHandler getConnection();

    @NotNull
    default ConnectionHandler ensureConnection() {
        return Failsafe.nn(getConnection());
    }

    @NotNull
    default DBObjectBundle getObjectBundle() {
        return ensureConnection().getObjectBundle();
    }

    @NotNull
    default DatabaseInterfaces getInterfaces() {
        return ensureConnection().getInterfaces();
    }

    default DatabaseInterfaceContext createInterfaceContext() {
        return DatabaseInterfaceContext.create(ensureConnection(), null, true);
    }
}
