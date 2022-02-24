package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public final class PooledConnection {
    private PooledConnection() {}

    public static void run(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @NotNull ParametricRunnable<DBNConnection, SQLException> runnable) throws SQLException {
        run(readonly, connection, null, runnable);
    }

    public static void run(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @Nullable SchemaId schemaId,
            @NotNull ParametricRunnable<DBNConnection, SQLException> runnable) throws SQLException {

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
            @NotNull ParametricCallable<DBNConnection, R, SQLException> callable) throws SQLException{
        return call(readonly, connection, null, callable);
    }

    public static <R> R call(
            boolean readonly,
            @NotNull ConnectionHandler connection,
            @Nullable SchemaId schemaId,
            @NotNull ParametricCallable<DBNConnection, R, SQLException> callable) throws SQLException{

        DBNConnection conn = null;
        try {
            connection.checkDisposed();
            conn = schemaId == null ?
                    connection.getPoolConnection(readonly) :
                    connection.getPoolConnection(schemaId, readonly);

            connection.checkDisposed();
            conn.set(ResourceStatus.ACTIVE, true);
            return callable.call(conn);

        } finally {
            if (conn != null) {
                connection.freePoolConnection(conn);
                conn.set(ResourceStatus.ACTIVE, false);
            }
        }
    }
}
