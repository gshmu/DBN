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
public final class ConnectionRef implements Reference<ConnectionHandler>, Identifiable<ConnectionId> {
    private static final Map<ConnectionId, ConnectionRef> registry = new ConcurrentHashMap<>();
    private final ConnectionId connectionId;
    private volatile boolean resolving;

    @EqualsAndHashCode.Exclude
    private WeakRef<ConnectionHandler> reference;

    private ConnectionRef(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public ConnectionId getId() {
        return connectionId;
    }

    @NotNull
    public ConnectionHandler ensure() {
        ConnectionHandler connection = get();
        return Failsafe.nn(connection);
    }


    @Nullable
    public ConnectionHandler get() {
        if (connectionId != null && !isValid()) {
            if (!resolving) {
                synchronized (this) {
                    if (!resolving) {
                        try {
                            resolving = true;
                            ConnectionHandler connection = ConnectionCache.resolveConnection(connectionId);
                            reference = WeakRef.of(connection);
                        } finally {
                            resolving = false;
                        }
                    }
                }
            }
        }
        return reference();
    }

    public boolean isValid() {
        return Failsafe.check(reference());
    }

    @Nullable
    private ConnectionHandler reference() {
        return reference == null ? null : reference.get();
    }

    /**************************************************************************
     *                         Static utilities                               *
     **************************************************************************/


    @Contract("null -> null;!null -> !null;")
    public static ConnectionRef of(@Nullable ConnectionHandler connection) {
        if (connection != null) {
            ConnectionRef ref = ConnectionRef.of(connection.getConnectionId());
            ConnectionHandler local = ref.get();
            if (local == null || local != connection) {
                ref.reference = WeakRef.of(connection);
            }
            return ref;
        }
        return null;
    }

    @NotNull
    public static ConnectionRef of(@NotNull ConnectionId connectionId) {
        return registry.computeIfAbsent(connectionId, id -> new ConnectionRef(id));
    }

    @Contract("null -> null;!null -> !null;")
    public static ConnectionHandler get(@Nullable ConnectionRef ref) {
        return ref == null ? null : ref.get();
    }

    @NotNull
    public static ConnectionHandler ensure(@NotNull ConnectionRef ref) {
        return Failsafe.nn(ref).ensure();
    }
}
