package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.util.Jdbc.Callable;
import com.dci.intellij.dbn.connection.util.Jdbc.Runnable;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

class DatabaseInterfaceContext {
    private static final ThreadLocal<ConnectionHandler> CONNECTION = new ThreadLocal<>();

    @NotNull
    public static ConnectionHandler getConnection() {
        return Failsafe.nd(CONNECTION.get());
    }

    static <T> T surround(ConnectionHandler connection, Callable<T> callable) throws SQLException {
        boolean initialised = init(connection);
        try {
            return callable.call();
            //} catch (ProcessCanceledException ignore){ // TODO
        } finally {
            release(initialised);
        }
    }

    static void surround(@NotNull ConnectionHandler connection, @NotNull Runnable runnable) throws SQLException {
        boolean initialised = init(connection);
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore){
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

        if (connection != localConnection) {
            throw new IllegalStateException("Already initialized for another connection");
        }

        return false;
    }

    private static void release(boolean initialised) {
        if (!initialised) return;
        CONNECTION.set(null);
    }
}
