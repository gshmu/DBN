package com.dci.intellij.dbn.connection;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public interface PresentableConnectionProvider extends ConnectionProvider, Presentable{
    @Nullable
    ConnectionHandler getConnectionHandler();
}
