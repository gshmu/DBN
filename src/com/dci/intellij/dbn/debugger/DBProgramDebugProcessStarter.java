package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.xdebugger.XDebugProcessStarter;

public abstract class DBProgramDebugProcessStarter extends XDebugProcessStarter {
    private ConnectionHandlerRef connectionHandlerRef;

    public DBProgramDebugProcessStarter(ConnectionHandler connectionHandler) {
        connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }
}
