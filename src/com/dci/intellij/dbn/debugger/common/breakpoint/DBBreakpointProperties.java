package com.dci.intellij.dbn.debugger.common.breakpoint;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaBreakpointProperties;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.util.xmlb.annotations.Attribute;

public class DBBreakpointProperties extends JavaBreakpointProperties<DBBreakpointProperties> {
    @Attribute("connection-id")
    private String connectionId;

    private ConnectionHandlerRef connectionHandlerRef;

    public DBBreakpointProperties() {
        System.out.println();
    }

    public DBBreakpointProperties(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        if (connectionHandler != null) {
            connectionId = connectionHandler.getId();
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public ConnectionHandler getConnectionHandler() {
        if (connectionHandlerRef == null && connectionId != null) {
            connectionHandlerRef = new ConnectionHandlerRef(connectionId);
        }
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }

    @Nullable
    @Override
    public DBBreakpointProperties getState() {
        return super.getState();
    }

    @Override
    public void loadState(DBBreakpointProperties state) {
        super.loadState(state);
        connectionId = state.connectionId;
        connectionHandlerRef = state.connectionHandlerRef;
    }
}
