package com.dci.intellij.dbn.debugger.jdbc;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.Nullable;

public class DBJdbcBreakpointProperties extends XBreakpointProperties<DBJdbcBreakpointProperties> implements DBBreakpointProperties {
    @Attribute(value = "connection-id", converter = ConnectionId.Converter.class)
    private ConnectionId connectionId;

    private ConnectionHandlerRef connectionHandlerRef;

    public DBJdbcBreakpointProperties() {
    }

    public DBJdbcBreakpointProperties(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = ConnectionHandlerRef.of(connectionHandler);
        if (connectionHandler != null) {
            connectionId = connectionHandler.getConnectionId();
        }
    }

    @Override
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Override
    @Nullable
    public ConnectionHandler getConnectionHandler() {
        if (connectionHandlerRef == null && connectionId != null) {
            connectionHandlerRef = ConnectionHandlerRef.of(connectionId);
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
