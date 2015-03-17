package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.progress.ProcessCanceledException;

public class ConnectionHandlerRef{
    private WeakReference<ConnectionHandler> reference;
    private String connectionId;

    public ConnectionHandlerRef(ConnectionHandler connectionHandler) {
        reference = new WeakReference<ConnectionHandler>(connectionHandler);
        connectionId = connectionHandler == null ? null : connectionHandler.getId();
    }

    @NotNull
    public ConnectionHandler get() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        if ((connectionHandler == null || connectionHandler.isDisposed()) && connectionId != null) {
            connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            reference = new WeakReference<ConnectionHandler>(connectionHandler);
        }

        if (connectionHandler == null || connectionHandler.isDisposed()) {
            throw new ProcessCanceledException();
        }
        return connectionHandler;
    }

    public static ConnectionHandlerRef from(ConnectionHandler connectionHandler) {
        return connectionHandler == null ? null : connectionHandler.getRef();
    }

    public static ConnectionHandler get(ConnectionHandlerRef connectionHandlerRef) {
        return connectionHandlerRef == null ? null :connectionHandlerRef.get();
    }
}
