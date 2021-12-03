package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.xdebugger.XDebugProcessStarter;

public abstract class DBDebugProcessStarter extends XDebugProcessStarter {
    private ConnectionHandlerRef connectionHandlerRef;

    public DBDebugProcessStarter(ConnectionHandler connectionHandler) {
        connectionHandlerRef = ConnectionHandlerRef.of(connectionHandler);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.ensure();
    }
}
