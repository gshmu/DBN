package com.dci.intellij.dbn.debugger.common.breakpoint;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import org.jetbrains.annotations.Nullable;

public interface DBBreakpointProperties {
    ConnectionId getConnectionId();

    @Nullable
    ConnectionHandler getConnection();
}
