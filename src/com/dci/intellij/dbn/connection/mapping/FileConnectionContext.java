package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.context.ConnectionContextProvider;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public interface FileConnectionContext extends ConnectionContextProvider, PersistentStateElement {
    String getFileUrl();

    @Nullable
    ConnectionId getConnectionId();

    @Nullable
    SessionId getSessionId();

    default String getSchemaName() {
        return Safe.call(getSchemaId(), s -> s.getName());
    }

    @Nullable
    VirtualFile getFile();

    default void setFileUrl(String fileUrl) {
        throw new UnsupportedOperationException();
    }

    default boolean setConnectionId(@Nullable ConnectionId connectionId) {
        throw new UnsupportedOperationException();
    }

    default boolean setSessionId(@Nullable SessionId sessionId) {
        throw new UnsupportedOperationException();
    }

    default boolean setSchemaId(@Nullable SchemaId schemaId) {
        throw new UnsupportedOperationException();
    }

}
