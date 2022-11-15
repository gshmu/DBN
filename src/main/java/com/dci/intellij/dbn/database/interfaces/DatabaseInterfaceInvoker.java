package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.cache.CacheKey;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionLocalContext;
import com.dci.intellij.dbn.connection.PooledConnection;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Callable;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Runnable;

import java.sql.SQLException;

import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionCallable;
import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionRunnable;

public final class DatabaseInterfaceInvoker {
    private DatabaseInterfaceInvoker() {}

    public static <T> T call(DatabaseInterfaceContext context, Callable<T> callable) throws SQLException {
        return ConnectionLocalContext.surround(context, callable);
    }

    public static void run(DatabaseInterfaceContext context, Runnable runnable) throws SQLException {
        ConnectionLocalContext.surround(context, runnable);
    }

    public static void run(DatabaseInterfaceContext context, ConnectionRunnable runnable) throws SQLException {
        ConnectionLocalContext.surround(context, () -> PooledConnection.run(context, runnable));
    }

    public static <T> T call(DatabaseInterfaceContext context, ConnectionCallable<T> callable) throws SQLException {
        return ConnectionLocalContext.surround(context, () -> PooledConnection.call(context, callable));
    }

    public static <T> T cached(CacheKey<T> key, Callable<T> loader) throws SQLException{
        return ConnectionHandler.local().getMetaDataCache().get(key, loader);
    }
}
