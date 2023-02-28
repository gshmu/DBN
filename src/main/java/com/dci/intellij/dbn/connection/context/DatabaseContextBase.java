package com.dci.intellij.dbn.connection.context;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaces;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.dci.intellij.dbn.common.util.Commons.nvl;

public interface DatabaseContextBase extends DatabaseContext{

    @Nullable
    default ConnectionId getConnectionId() {
        ConnectionHandler connection = getConnection();
        return connection == null ? null : connection.getConnectionId();
    }

    @Nullable
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

    @Override
    default boolean isSameAs(DatabaseContext context) {
        return
            Objects.equals(nvl(getConnectionId(), ConnectionId.NULL), nvl(context.getConnectionId(), ConnectionId.NULL)) &&
            Objects.equals(nvl(getSessionId(), SessionId.NULL), nvl(context.getSessionId(), SessionId.NULL)) &&
            Objects.equals(nvl(getSchemaId(), SchemaId.NULL), nvl(context.getSchemaId(), SchemaId.NULL));
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

    default ConnectionContext createConnectionContext() {
        return new ConnectionContext(getProject(), getConnectionId(), null);
    }


}
