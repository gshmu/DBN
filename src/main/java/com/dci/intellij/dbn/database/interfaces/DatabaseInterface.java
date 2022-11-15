package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.cache.CacheKey;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.PooledConnection;
import com.dci.intellij.dbn.connection.util.Jdbc.Callable;
import com.dci.intellij.dbn.connection.util.Jdbc.ConnectionCallable;
import com.dci.intellij.dbn.connection.util.Jdbc.ConnectionRunnable;
import com.dci.intellij.dbn.connection.util.Jdbc.Runnable;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

public interface DatabaseInterface {

    SQLNonTransientConnectionException DBN_NOT_CONNECTED_EXCEPTION = new SQLNonTransientConnectionException("Not connected to database");

    default void reset() {}

    static ConnectionHandler getConnection() {
        return DatabaseInterfaceContext.getConnection();
    }

    static <T, E extends Throwable> T cached(CacheKey<T> key, Callable<T> loader) throws SQLException{
        return getConnection().getMetaDataCache().get(key, loader);
    }

    static <T> T call(ConnectionHandler connection, Callable<T> callable) throws SQLException {
        return DatabaseInterfaceContext.surround(connection, callable);
    }

    static void run(ConnectionHandler connection, Runnable runnable) throws SQLException {
        DatabaseInterfaceContext.surround(connection, runnable);
    }

    static void run(
            boolean readonly,
            ConnectionHandler connection,
            ConnectionRunnable runnable) throws SQLException {

        DatabaseInterfaceContext.surround(connection, () ->
                PooledConnection.run(
                        readonly,
                        connection,
                        runnable));
    }

    static <T> T call(
            boolean readonly,
            ConnectionHandler connection,
            ConnectionCallable<T> callable) throws SQLException {

        return DatabaseInterfaceContext.surround(connection, () ->
                PooledConnection.call(
                        readonly,
                        connection,
                        callable));
    }
}
