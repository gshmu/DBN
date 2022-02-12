package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import org.jetbrains.annotations.Nullable;

public interface FileConnectionMappingProvider extends ConnectionProvider {
    @Nullable
    DatabaseSession getSession();

    @Nullable
    SchemaId getSchemaId();

}
