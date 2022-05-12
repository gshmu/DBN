package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.common.cache.CacheKey;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.PooledConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

public interface DatabaseInterface {
    ThreadLocal<ConnectionHandler> CONNECTION_HANDLER = new ThreadLocal<>();

    SQLNonTransientConnectionException DBN_NOT_CONNECTED_EXCEPTION = new SQLNonTransientConnectionException("Not connected to database");

    default void reset(){};

    static boolean init(ConnectionHandler connection) {
        ConnectionHandler localConnectionHandler = CONNECTION_HANDLER.get();
        if (localConnectionHandler == null) {
            CONNECTION_HANDLER.set(connection);
            return true;
        } else {
            if (connection != localConnectionHandler) {
                throw new IllegalStateException("Already initialized for another connection");
            }
        }
        return false;
    }

    static void release(boolean owner) {
        if (owner) {
            CONNECTION_HANDLER.set(null);
        }
    }

    static Cache cache() {
        return getConnection().getMetaDataCache();
    }

    @NotNull
    static ConnectionHandler getConnection() {
        return Failsafe.nd(CONNECTION_HANDLER.get());
    }

    static <T, E extends Throwable> T cached(CacheKey<T> key, ThrowableCallable<T, E> loader) throws E{
        return cache().get(key, loader);
    }

    static void run(
            @NotNull ConnectionHandler connection,
            @NotNull ParametricRunnable<DatabaseInterfaceProvider, SQLException> runnable) throws SQLException {
        boolean owner = init(connection);
        try {
            runnable.run(connection.getInterfaceProvider());
        } catch (ProcessCanceledException ignore){
        } finally {
            release(owner);
        }
    }

    static void run(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @NotNull Runnable<SQLException> runnable) throws SQLException {

        run(connection, provider -> PooledConnection.run(
                readonly,
                connection,
                conn -> runnable.run(
                        connection.getInterfaceProvider(),
                        conn)));
    }

    static <T> T call(
            @NotNull ConnectionHandler connection,
            @NotNull ParametricCallable<DatabaseInterfaceProvider, T, SQLException> callable) throws SQLException {

        boolean owner = init(connection);
        try {
            return callable.call(connection.getInterfaceProvider());
            //} catch (ProcessCanceledException ignore){ // TODO
        } finally {
            release(owner);
        }
    }

    static <T> T call(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @NotNull Callable<T, SQLException> callable) throws SQLException {

        return call(connection, provider ->
                PooledConnection.call(
                        readonly,
                        connection,
                        conn -> callable.call(
                                connection.getInterfaceProvider(),
                                conn)));
    }

    @FunctionalInterface
    interface Runnable<E extends Throwable> {
        void run(
                @NotNull DatabaseInterfaceProvider provider,
                @NotNull DBNConnection connection) throws E;
    }

    @FunctionalInterface
    interface Callable<T, E extends Throwable> {
        T call(
                @NotNull DatabaseInterfaceProvider provider,
                @NotNull DBNConnection connection) throws E;
    }
}
