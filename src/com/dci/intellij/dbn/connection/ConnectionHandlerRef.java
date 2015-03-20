package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;

public class ConnectionHandlerRef{
    private WeakReference<ConnectionHandler> reference;
    private String connectionId;

    public ConnectionHandlerRef(ConnectionHandler connectionHandler) {
        reference = new WeakReference<ConnectionHandler>(connectionHandler);
        connectionId = connectionHandler == null ? null : connectionHandler.getId();
    }

    public String getConnectionId() {
        return connectionId;
    }

    @NotNull
    public ConnectionHandler get() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        if ((connectionHandler == null || connectionHandler.isDisposed()) && connectionId != null) {
            connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            reference = new WeakReference<ConnectionHandler>(connectionHandler);
        }

        return FailsafeUtil.get(connectionHandler);
    }

    public static ConnectionHandlerRef from(ConnectionHandler connectionHandler) {
        return connectionHandler == null ? null : connectionHandler.getRef();
    }

    public static ConnectionHandler get(ConnectionHandlerRef connectionHandlerRef) {
        return connectionHandlerRef == null ? null :connectionHandlerRef.get();
    }
}
