package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.connection.ConnectionHandler;

import javax.swing.Icon;

public interface DBVirtualFile {
    Icon getIcon();
    ConnectionHandler getConnectionHandler();
    void release();
}
