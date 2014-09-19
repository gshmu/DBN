package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.Disposable;

public interface ConnectionProvider extends Disposable {
    ConnectionHandler getConnectionHandler();
}
