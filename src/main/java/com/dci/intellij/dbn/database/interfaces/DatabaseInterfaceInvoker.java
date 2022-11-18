package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.cache.CacheKey;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionLocalContext;
import com.dci.intellij.dbn.connection.PooledConnection;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Callable;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceContext;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskDefinition;

import java.sql.SQLException;

import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionCallable;
import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionRunnable;

public final class DatabaseInterfaceInvoker {
    private DatabaseInterfaceInvoker() {
    }

    /**
     * Database Interface invocation against a pool connection
     * Schedules the task and returns immediately
     *
     * @param taskDefinition information about the task (title, description, priority, connectivity context)
     * @param runnable       the task to be executed
     * @throws SQLException if jdbc call fails
     */
    public static void schedule(InterfaceTaskDefinition taskDefinition, ConnectionRunnable runnable) throws SQLException {
        InterfaceContext context = taskDefinition.getContext();
        DatabaseInterfaceQueue interfaceQueue = context.getConnection().getInterfaceQueue();
        interfaceQueue.scheduleAndForget(
                taskDefinition, () -> ConnectionLocalContext.surround(context,
                        () -> PooledConnection.run(context, runnable)));
    }

    /**
     * Database Interface invocation against a pool connection
     * Schedules the task and waits for the execution
     *
     * @param taskDefinition information about the task (title, description, priority, connectivity context)
     * @param runnable       the task to be executed
     * @throws SQLException if jdbc call fails
     */
    public static void execute(InterfaceTaskDefinition taskDefinition, ConnectionRunnable runnable) throws SQLException {
        InterfaceContext context = taskDefinition.getContext();
        DatabaseInterfaceQueue interfaceQueue = context.getConnection().getInterfaceQueue();
        interfaceQueue.scheduleAndWait(
                taskDefinition, () -> ConnectionLocalContext.surround(context,
                        () -> PooledConnection.run(context, runnable)));
    }

    /**
     * Database Interface invocation against a pool connection
     *
     * @param <T>        type of the entity returned by the invocation
     * @param definition information about the task (title, description, priority, connectivity context)
     * @param callable   the task to be executed
     * @return an entity returned by the callable
     * @throws SQLException if jdbc call fails
     */
    public static <T> T load(InterfaceTaskDefinition definition, ConnectionCallable<T> callable) throws SQLException {
        InterfaceContext context = definition.getContext();
        DatabaseInterfaceQueue interfaceQueue = context.getConnection().getInterfaceQueue();
        return interfaceQueue.scheduleAndReturn(
                definition, () -> ConnectionLocalContext.surround(context,
                        () -> PooledConnection.call(context, callable)));
    }

    public static <T> T cached(CacheKey<T> key, Callable<T> loader) throws SQLException {
        return ConnectionHandler.local().getMetaDataCache().get(key, loader);
    }
}
