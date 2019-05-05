package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface DatabaseInterface {
    ThreadLocal<ConnectionHandler> CONNECTION_HANDLER = new ThreadLocal<>();

    SQLException DBN_NOT_CONNECTED_EXCEPTION = new SQLException("Not connected to database");

    default void reset(){};


    static <E extends Throwable> void execute(ConnectionHandler connectionHandler, ThrowableRunnable<E> runnable) throws E {
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
            runnable.run();
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
