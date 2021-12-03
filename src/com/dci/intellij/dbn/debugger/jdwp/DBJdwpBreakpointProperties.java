package com.dci.intellij.dbn.debugger.jdwp;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaBreakpointProperties;

public class DBJdwpBreakpointProperties extends JavaBreakpointProperties<DBJdwpBreakpointProperties> implements DBBreakpointProperties {
    @Attribute(value = "connection-id", converter = ConnectionId.Converter.class)
    private ConnectionId connectionId;

    private ConnectionHandlerRef connectionHandlerRef;

    public DBJdwpBreakpointProperties() {
        System.out.println();
    }

    public DBJdwpBreakpointProperties(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = ConnectionHandlerRef.of(connectionHandler);
        if (connectionHandler != null) {
            connectionId = connectionHandler.getConnectionId();
        }
    }

    @Override
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        if (connectionHandlerRef == null && connectionId != null) {
            connectionHandlerRef = new ConnectionHandlerRef(connectionId);
        }
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }

    @Nullable
    @Override
    public DBJdwpBreakpointProperties getState() {
        return super.getState();
    }

    @Override
    public void loadState(@NotNull DBJdwpBreakpointProperties state) {
        super.loadState(state);
        connectionId = state.connectionId;
        connectionHandlerRef = state.connectionHandlerRef;
    }
}
