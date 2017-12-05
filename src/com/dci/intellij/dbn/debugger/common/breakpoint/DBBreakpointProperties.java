package com.dci.intellij.dbn.debugger.common.breakpoint;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;

public interface DBBreakpointProperties {
    ConnectionId getConnectionId();

    @Nullable
    ConnectionHandler getConnectionHandler();
}
