package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;

public class ConnectionHandlerRef{
    private WeakReference<ConnectionHandler> reference;
    private String connectionId;

    public ConnectionHandlerRef(ConnectionHandler connectionHandler) {
        reference = new WeakReference<ConnectionHandler>(connectionHandler);
        connectionId = connectionHandler == null ? null : connectionHandler.getId();
    }

    public ConnectionHandler get() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        if (connectionHandler == null && connectionId != null) {
            connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            reference = new WeakReference<ConnectionHandler>(connectionHandler);
        }
        return connectionHandler;
    }

    public void release() {
        reference = null;
    }

}
