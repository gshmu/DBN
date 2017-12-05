package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeProvider;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;

public interface DBVirtualFile extends /*VirtualFileWithId, */EnvironmentTypeProvider, ConnectionProvider, UserDataHolder, Disposable {
    @Nullable
    Project getProject();

    Icon getIcon();

    @NotNull
    ConnectionId getConnectionId();

    @NotNull
    ConnectionHandler getConnectionHandler();

    DBSchema getCurrentSchema();
}