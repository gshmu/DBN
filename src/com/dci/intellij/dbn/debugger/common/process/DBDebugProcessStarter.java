package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.xdebugger.XDebugProcessStarter;

public abstract class DBDebugProcessStarter extends XDebugProcessStarter {
    private final ConnectionHandlerRef connection;

    public DBDebugProcessStarter(ConnectionHandler connection) {
        this.connection = ConnectionHandlerRef.of(connection);
    }

    public ConnectionHandler getConnection() {
        return connection.ensure();
    }
}
