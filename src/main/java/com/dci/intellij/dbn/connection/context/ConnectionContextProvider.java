package com.dci.intellij.dbn.connection.context;

import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import org.jetbrains.annotations.Nullable;

public interface ConnectionContextProvider extends ConnectionProvider {
    @Nullable
    DatabaseSession getSession();

    @Nullable
    SchemaId getSchemaId();

}
