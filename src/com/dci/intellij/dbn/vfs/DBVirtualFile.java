package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeProvider;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface DBVirtualFile extends /*VirtualFileWithId, */EnvironmentTypeProvider, FileConnectionMappingProvider, UserDataHolder, Disposable {
    @Nullable
    Project getProject();

    @NotNull
    Project ensureProject();

    Icon getIcon();

    @Override
    @NotNull
    ConnectionHandler getConnectionHandler();

    @NotNull
    ConnectionId getConnectionId();

    void setCachedViewProvider(@Nullable DatabaseFileViewProvider viewProvider);

    @Nullable
    DatabaseFileViewProvider getCachedViewProvider();
}