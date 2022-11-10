package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.environment.EnvironmentTypeProvider;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.context.ConnectionContextProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public interface DBVirtualFile extends /*VirtualFileWithId, */EnvironmentTypeProvider, ConnectionContextProvider, UserDataHolder {
    @NotNull
    Project getProject();

    Icon getIcon();

    @Override
    @NotNull
    ConnectionHandler getConnection();

    @NotNull
    ConnectionId getConnectionId();

    void setCachedViewProvider(@Nullable DatabaseFileViewProvider viewProvider);

    @Nullable
    DatabaseFileViewProvider getCachedViewProvider();

    void invalidate();

    @Nullable
    default DBObject getObject() {
        return null;
    }
}