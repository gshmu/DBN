package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;

import com.dci.intellij.dbn.connection.ConnectionProvider;

public interface DBVirtualFile extends ConnectionProvider {
    Icon getIcon();
}
