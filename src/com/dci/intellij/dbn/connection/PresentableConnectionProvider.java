package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.Nullable;

public interface PresentableConnectionProvider extends ConnectionProvider, Presentable{
    @Nullable
    ConnectionHandler getConnectionHandler();
}
