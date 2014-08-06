package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.Disposable;

public interface DBVirtualFile extends Disposable {
    Icon getIcon();
    ConnectionHandler getConnectionHandler();
    void dispose();
}
