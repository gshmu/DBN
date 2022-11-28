package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.database.interfaces.DatabaseInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Callable;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Runnable;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;

@Data
public class ConnectionContext {
    private final ConnectionId connectionId;
    private final SchemaId schemaId;

    public ConnectionContext(ConnectionId connectionId, SchemaId schemaId) {
        this.connectionId = connectionId;
        this.schemaId = schemaId;
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return ConnectionHandler.ensure(connectionId);
    }

    public static <T> T surround(ConnectionContext context, Callable<T> callable) throws SQLException {
        return Local.surround(context, callable);
    }

    public static void surround(ConnectionContext context, Runnable runnable) throws SQLException {
        Local.surround(context, runnable);
    }

    public static ConnectionContext local() {
        return Local.get();
    }

    /**
     * Thread local connection context
     */
    private static class Local {
        private static final ThreadLocal<ConnectionContext> LOCAL = new ThreadLocal<>();

        private Local() {}

        static ConnectionContext get() {
            ConnectionContext context = LOCAL.get();
            if (context == null) throw new IllegalStateException("Connection context not initialised");
            return context;
        }

        static <T> T surround(ConnectionContext context, Callable<T> callable) throws SQLException {
            boolean initialised = init(context);
            try {
                return callable.call();
            } finally {
                release(initialised);
            }
        }

        static void surround(ConnectionContext context, DatabaseInterface.Runnable runnable) throws SQLException {
            boolean initialised = init(context);
            try {
                runnable.run();
            } finally {
                release(initialised);
            }
        }

        static boolean init(ConnectionContext context) {
            ConnectionContext localContext = LOCAL.get();
            if (localContext == null) {
                LOCAL.set(context);
                return true;
            }

            if (!Objects.equals(context, localContext)) throw new IllegalStateException("Context already initialized for another connection");

            return false;
        }

        static void release(boolean initialised) {
            if (!initialised) return;
            LOCAL.set(null);
        }
    }
}
