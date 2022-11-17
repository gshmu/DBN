package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.cache.CacheKey;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionLocalContext;
import com.dci.intellij.dbn.connection.PooledConnection;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Callable;

import java.sql.SQLException;

import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionCallable;
import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionRunnable;

public final class DatabaseInterfaceInvoker {
    private DatabaseInterfaceInvoker() {}

    /**
     * Database Interface invocation against a pool connection
     *
     * @param context the connectivity context
     * @param runnable the task to be executed
     * @throws SQLException if jdbc call fails
     */
    public static void run(DatabaseInterfaceContext context, ConnectionRunnable runnable) throws SQLException {
        DatabaseInterfaceQueue interfaceQueue = context.getConnection().getInterfaceQueue();
        interfaceQueue.scheduleAndWait("DBN background task", Priority.MEDIUM,
                () -> ConnectionLocalContext.surround(context,
                        () -> PooledConnection.run(context, runnable)));
    }

    /**
     * Database Interface invocation against a pool connection
     *
     * @return an entity returned by the callable
     * @param context the connectivity context
     * @param callable the task to be executed
     * @param <T> type of the entity returned by the invocation
     * @throws SQLException if jdbc call fails
     */
    public static <T> T call(DatabaseInterfaceContext context, ConnectionCallable<T> callable) throws SQLException {
        DatabaseInterfaceQueue interfaceQueue = context.getConnection().getInterfaceQueue();
        return interfaceQueue.scheduleAndReturn("DBN background task", Priority.MEDIUM,
                () -> ConnectionLocalContext.surround(context,
                        () -> PooledConnection.call(context, callable)));
    }

    public static <T> T cached(CacheKey<T> key, Callable<T> loader) throws SQLException{
        return ConnectionHandler.local().getMetaDataCache().get(key, loader);
    }
}
