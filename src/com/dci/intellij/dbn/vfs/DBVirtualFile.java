package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.Disposable;

import javax.swing.Icon;

public interface DBVirtualFile extends Disposable {
    Icon getIcon();
    ConnectionHandler getConnectionHandler();
    void dispose();
}
