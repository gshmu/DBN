package com.dci.intellij.dbn.debugger.common.breakpoint;

import com.dci.intellij.dbn.connection.ConnectionHandler;

public interface DBBreakpointProperties {
    String getConnectionId();

    ConnectionHandler getConnectionHandler();
}
