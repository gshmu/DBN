package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Callable;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Runnable;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceContext;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Thread local connection context
 */
public final class ConnectionLocalContext {
    private static final ThreadLocal<ConnectionHandler> CONNECTION = new ThreadLocal<>();

    private ConnectionLocalContext() {}

    @NotNull
    public static ConnectionHandler getConnection() {
        ConnectionHandler connection = CONNECTION.get();
        if (connection == null) throw new IllegalStateException("Connection context not initialised");
        return Failsafe.nd(connection);
    }

    public static <T> T surround(@NotNull DatabaseInterfaceContext context, @NotNull Callable<T> callable) throws SQLException {
        boolean initialised = init(context.getConnection());
        try {
            return callable.call();
        } finally {
            release(initialised);
        }
    }

    public static void surround(@NotNull DatabaseInterfaceContext context, @NotNull Runnable runnable) throws SQLException {
        boolean initialised = init(context.getConnection());
        try {
            runnable.run();
        } finally {
            release(initialised);
        }
    }

    private static boolean init(ConnectionHandler connection) {
        ConnectionHandler localConnection = CONNECTION.get();
        if (localConnection == null) {
            CONNECTION.set(connection);
            return true;
        }

        if (connection != localConnection) throw new IllegalStateException("Context already initialized for another connection");

        return false;
    }

    private static void release(boolean initialised) {
        if (!initialised) return;
        CONNECTION.set(null);
    }
}
