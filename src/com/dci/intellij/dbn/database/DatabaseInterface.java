package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLNonTransientConnectionException;

public interface DatabaseInterface {
    ThreadLocal<ConnectionHandler> CONNECTION_HANDLER = new ThreadLocal<>();

    SQLNonTransientConnectionException DBN_NOT_CONNECTED_EXCEPTION = new SQLNonTransientConnectionException("Not connected to database");

    default void reset(){};


    static <E extends Throwable> void run(
            @NotNull ConnectionProvider connectionProvider,
            @NotNull ParametricRunnable<DatabaseInterfaceProvider, E> runnable) throws E {
        ConnectionHandler connectionHandler = connectionProvider.ensureConnectionHandler();
        boolean owner = false;
        try {
            // init
            ConnectionHandler localConnectionHandler = CONNECTION_HANDLER.get();
            if (localConnectionHandler == null) {
                CONNECTION_HANDLER.set(connectionHandler);
                owner = true;
            } else {
                if (connectionHandler != localConnectionHandler) {
                    throw new IllegalStateException("Already initialized for another connection");
                }
            }

            // execute
            DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
            runnable.run(interfaceProvider);
        } finally {

            // release
            if (owner) {
                CONNECTION_HANDLER.set(null);
            }
        }
    }

    static <R, E extends Throwable> R call(
            @NotNull ConnectionProvider connectionProvider,
            @NotNull ParametricCallable<DatabaseInterfaceProvider, R, E> callable) throws E{

        ConnectionHandler connectionHandler = connectionProvider.ensureConnectionHandler();
        boolean owner = false;
        try {
            // init
            ConnectionHandler localConnectionHandler = CONNECTION_HANDLER.get();
            if (localConnectionHandler == null) {
                CONNECTION_HANDLER.set(connectionHandler);
                owner = true;
            } else {
                if (connectionHandler != localConnectionHandler) {
                    throw new IllegalStateException("Already initialized for another connection");
                }
            }

            // execute
            DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
            return callable.call(interfaceProvider);
        } finally {

            // release
            if (owner) {
                CONNECTION_HANDLER.set(null);
            }
        }
    }


    static Cache cache() {
        return connectionHandler().getMetaDataCache();
    }

    @NotNull
    static ConnectionHandler connectionHandler() {
        return Failsafe.nd(CONNECTION_HANDLER.get());
    }

    default <T, E extends Throwable> T cached(String key, ThrowableCallable<T, E> loader) throws E{
        return cache().get(key, loader);
    }
}
