package com.dci.intellij.dbn.debugger.jdbc;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

public class DBJdbcBreakpointProperties extends XBreakpointProperties<DBJdbcBreakpointProperties> implements DBBreakpointProperties {
    @Attribute("connection-id")
    private String connectionId;

    private ConnectionHandlerRef connectionHandlerRef;

    public DBJdbcBreakpointProperties() {
    }

    public DBJdbcBreakpointProperties(ConnectionHandler connectionHandler) {
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
    public DBJdbcBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(DBJdbcBreakpointProperties state) {
        connectionId = state.connectionId;
        connectionHandlerRef = state.connectionHandlerRef;
    }
}
