package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.connection.util.Jdbc.ConnectionCallable;
import com.dci.intellij.dbn.connection.util.Jdbc.ConnectionRunnable;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public final class PooledConnection {
    private PooledConnection() {}

    public static void run(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @NotNull ConnectionRunnable runnable) throws SQLException {
        run(readonly, connection, null, runnable);
    }

    public static void run(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @Nullable SchemaId schemaId,
            @NotNull ConnectionRunnable runnable) throws SQLException {

        DBNConnection conn = null;
        try {
            connection.checkDisposed();
            conn = schemaId == null ?
                    connection.getPoolConnection(readonly) :
                    connection.getPoolConnection(schemaId, readonly);

            connection.checkDisposed();
            conn.set(ResourceStatus.ACTIVE, true);
            runnable.run(conn);

        } catch (ProcessCanceledException ignore){
        } finally {
            if (conn != null) {
                connection.freePoolConnection(conn);
                conn.set(ResourceStatus.ACTIVE, false);
            }
        }
    }

    public static <R> R call(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @NotNull ConnectionCallable<R> callable) throws SQLException{
        return call(readonly, connection, null, callable);
    }

    public static <R> R call(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @Nullable SchemaId schemaId,
            @NotNull ConnectionCallable<R> callable) throws SQLException{

        DBNConnection c = null;
        try {
            connection.checkDisposed();
            c = schemaId == null ?
                    connection.getPoolConnection(readonly) :
                    connection.getPoolConnection(schemaId, readonly);

            connection.checkDisposed();
            c.set(ResourceStatus.ACTIVE, true);
            return callable.call(c);

        } finally {
            if (c != null) {
                connection.freePoolConnection(c);
                c.set(ResourceStatus.ACTIVE, false);
            }
        }
    }
}
