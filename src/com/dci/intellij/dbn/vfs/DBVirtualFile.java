package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;

public interface DBVirtualFile extends ConnectionProvider, Disposable {
    Icon getIcon();

    @NotNull
    ConnectionHandler getConnectionHandler();
}