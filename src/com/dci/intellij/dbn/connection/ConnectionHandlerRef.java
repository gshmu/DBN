package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Reference;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.language.common.WeakRef;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode
public class ConnectionHandlerRef implements Reference<ConnectionHandler> {
    private final ConnectionId connectionId;

    @EqualsAndHashCode.Exclude
    private WeakRef<ConnectionHandler> reference;

    public ConnectionHandlerRef(@Nullable ConnectionHandler connectionHandler) {
        reference = WeakRef.of(connectionHandler);
        connectionId = connectionHandler == null ? null : connectionHandler.getConnectionId();
    }

    public ConnectionHandlerRef(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @NotNull
    public ConnectionHandler ensure() {
        ConnectionHandler connectionHandler = get();
        return Failsafe.nn(connectionHandler);
    }

    @Nullable
    public ConnectionHandler get() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        if (!Failsafe.check(connectionHandler) && connectionId != null) {
            connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            reference = WeakRef.of(connectionHandler);
        }
        return connectionHandler;
    }

    @Contract("null -> null;!null -> !null;")
    public static ConnectionHandlerRef of(@Nullable ConnectionHandler connectionHandler) {
        return connectionHandler == null ? null : connectionHandler.getRef();
    }

    @Contract("null -> null;!null -> !null;")
    public static ConnectionHandler get(@Nullable ConnectionHandlerRef connectionHandlerRef) {
        return connectionHandlerRef == null ? null : connectionHandlerRef.get();
    }

    @NotNull
    public static ConnectionHandler ensure(@NotNull ConnectionHandlerRef connectionHandlerRef) {
        return Failsafe.nn(connectionHandlerRef).ensure();
    }

    public boolean isValid() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        return Failsafe.check(connectionHandler);
    }
}
