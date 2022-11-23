package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.environment.EnvironmentTypeProvider;
import com.dci.intellij.dbn.connection.context.DatabaseContextBase;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface DBVirtualFile extends /*VirtualFileWithId, */EnvironmentTypeProvider, DatabaseContextBase, UserDataHolder {
    @NotNull
    Project getProject();

    Icon getIcon();

    void setCachedViewProvider(@Nullable DatabaseFileViewProvider viewProvider);

    @Nullable
    DatabaseFileViewProvider getCachedViewProvider();

    void invalidate();

    @Nullable
    default DBObject getObject() {
        return null;
    }
}