package com.dci.intellij.dbn.debugger.common.breakpoint;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;

public interface DBBreakpointProperties {
    String getConnectionId();

    @Nullable
    ConnectionHandler getConnectionHandler();
}
