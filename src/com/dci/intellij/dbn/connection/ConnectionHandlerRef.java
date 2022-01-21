package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Reference;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.index.Identifiable;
import com.dci.intellij.dbn.language.common.WeakRef;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@EqualsAndHashCode
public final class ConnectionHandlerRef implements Reference<ConnectionHandler>, Identifiable<ConnectionId> {
    private static final Map<ConnectionId, ConnectionHandlerRef> REGISTRY = new ConcurrentHashMap<>();
    private final ConnectionId connectionId;

    @EqualsAndHashCode.Exclude
    private WeakRef<ConnectionHandler> reference;

    private ConnectionHandlerRef(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public ConnectionId getId() {
        return connectionId;
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
        if (connectionHandler != null) {
            ConnectionHandlerRef ref = ConnectionHandlerRef.of(connectionHandler.getConnectionId());
            ConnectionHandler local = ref.get();
            if (local == null || local != connectionHandler) {
                ref.reference = WeakRef.of(connectionHandler);
            }
            return ref;
        }
        return null;
    }

    @NotNull
    public static ConnectionHandlerRef of(@NotNull ConnectionId connectionId) {
        return REGISTRY.computeIfAbsent(connectionId, id -> new ConnectionHandlerRef(id));
    }

    @Contract("null -> null;!null -> !null;")
    public static ConnectionHandler get(@Nullable ConnectionHandlerRef ref) {
        return ref == null ? null : ref.get();
    }

    @NotNull
    public static ConnectionHandler ensure(@NotNull ConnectionHandlerRef ref) {
        return Failsafe.nn(ref).ensure();
    }

    public boolean isValid() {
        ConnectionHandler connectionHandler = reference == null ? null : reference.get();
        return Failsafe.check(connectionHandler);
    }
}
