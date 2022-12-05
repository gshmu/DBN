package com.dci.intellij.dbn.connection.context;

import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaces;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfacesProvider;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DatabaseContext extends DatabaseInterfacesProvider {

    @Nullable
    default Project getProject() {return null;}

    @Nullable
    ConnectionId getConnectionId();

    @Nullable
    SessionId getSessionId();

    @Nullable
    SchemaId getSchemaId();

    DBSchema getSchema();

    @Nullable
    String getSchemaName();

    @Nullable
    DatabaseSession getSession();

    @Nullable
    ConnectionHandler getConnection();

    @NotNull
    ConnectionHandler ensureConnection();

    @NotNull
    DBObjectBundle getObjectBundle();

    @NotNull
    DatabaseInterfaces getInterfaces();

    ConnectionContext createConnectionContext();
}
