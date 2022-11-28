package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.cache.CacheKey;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.Callable;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskRequest;

import java.sql.SQLException;

import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionCallable;
import static com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionRunnable;

public final class DatabaseInterfaceInvoker {
    private DatabaseInterfaceInvoker() {
    }

    /**
     * Database Interface invocation against a pool connection
     * Schedules the task and returns immediately
     */
    public static void schedule(Priority priority, String title, String text, ConnectionId connectionId, ConnectionRunnable runnable) throws SQLException {
        InterfaceTaskRequest request = InterfaceTaskRequest.create(priority, title, text, connectionId, null);
        ConnectionHandler connection = request.getConnection();
        DatabaseInterfaceQueue interfaceQueue = connection.getInterfaceQueue();

        interfaceQueue.scheduleAndForget(request,
                () -> ConnectionContext.surround(request,
                        () -> PooledConnection.run(request, runnable)));
    }


    /**
     * Database Interface invocation against a pool connection
     * Schedules the task and waits for the execution
     *
     * @param priority the priority of the task
     * @param connectionId the connection to be invoked against
     * @param runnable the task to be executed
     * @throws SQLException if jdbc call fails
     */
    public static void execute(Priority priority, ConnectionId connectionId, ConnectionRunnable runnable) throws SQLException {
        execute(priority, null, null, connectionId, runnable);
    }

    public static void execute(Priority priority, String title, String text, ConnectionId connectionId, ConnectionRunnable runnable) throws SQLException {
        execute(priority, title, text, connectionId, null, runnable);
    }

    /**
     * @deprecated use {@link #execute(Priority, String, String, ConnectionId, ConnectionRunnable)}
     */
    public static void execute(Priority priority, String title, String text, ConnectionId connectionId, @Deprecated SchemaId schemaId, ConnectionRunnable runnable) throws SQLException {
        InterfaceTaskRequest request = InterfaceTaskRequest.create(priority, title, text, connectionId, schemaId);
        ConnectionHandler connection = request.getConnection();
        DatabaseInterfaceQueue interfaceQueue = connection.getInterfaceQueue();

        interfaceQueue.scheduleAndWait(request,
                () -> ConnectionContext.surround(request,
                        () -> PooledConnection.run(request, runnable)));    }


    /**
     * Database Interface invocation against a pool connection
     * Schedules the task, waits for the execution and returns result
     *
     * @param <T>        type of the entity returned by the invocation
     * @param priority the priority of the task
     * @param connectionId the connection to be invoked against
     * @param callable   the task to be executed
     * @throws SQLException if jdbc call fails
     */
    public static <T> T load(Priority priority, ConnectionId connectionId, ConnectionCallable<T> callable) throws SQLException {
        return load(priority, null, null, connectionId, callable);
    }

    public static <T> T load(Priority priority, String title, String text, ConnectionId connectionId, ConnectionCallable<T> callable) throws SQLException {
        InterfaceTaskRequest request = InterfaceTaskRequest.create(priority, title, text, connectionId, null);
        ConnectionHandler connection = request.getConnection();
        DatabaseInterfaceQueue interfaceQueue = connection.getInterfaceQueue();
        return interfaceQueue.scheduleAndReturn(request,
                () -> ConnectionContext.surround(request,
                        () -> PooledConnection.call(request, callable)));
    }

    public static <T> T cached(CacheKey<T> key, Callable<T> loader) throws SQLException {
        return ConnectionHandler.local().getMetaDataCache().get(key, () -> loader.call());
    }
}
