package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class ConnectionHandlerRef{
    private WeakReference<ConnectionHandler> reference;
    private ConnectionId connectionId;

    public ConnectionHandlerRef(ConnectionHandler connectionHandler) {
        reference = new WeakReference<ConnectionHandler>(connectionHandler);
        connectionId = connectionHandler == null ? null : connectionHandler.getId();
    }

    public ConnectionHandlerRef(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @NotNull
    public ConnectionHandler getnn() {
        ConnectionHandler connectionHandler = get();
        return FailsafeUtil.get(connectionHandler);
    }

    @Nullable
    public ConnectionHandler get() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        if ((connectionHandler == null || connectionHandler.isDisposed()) && connectionId != null) {
            connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            reference = new WeakReference<>(connectionHandler);
        }
        return connectionHandler;
    }

    @Nullable
    public static ConnectionHandlerRef from(@Nullable ConnectionHandler connectionHandler) {
        return connectionHandler == null ? null : connectionHandler.getRef();
    }

    @Nullable
    public static ConnectionHandler get(@Nullable ConnectionHandlerRef connectionHandlerRef) {
        return connectionHandlerRef == null ? null :connectionHandlerRef.get();
    }

    @NotNull
    public static ConnectionHandler getnn(@NotNull ConnectionHandlerRef connectionHandlerRef) {
        return FailsafeUtil.get(connectionHandlerRef).getnn();
    }

    public boolean isValid() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        return connectionHandler != null && !connectionHandler.isDisposed();
    }
}
