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
            @NotNull ConnectionHandler connectionHandler,
            @NotNull ParametricRunnable<DBNConnection, SQLException> runnable) throws SQLException {
        run(readonly, connectionHandler, null, runnable);
    }

    public static void run(
            boolean readonly,
            @NotNull ConnectionHandler connectionHandler,
            @Nullable SchemaId schemaId,
            @NotNull ParametricRunnable<DBNConnection, SQLException> runnable) throws SQLException {

        DBNConnection connection = null;
        try {
            connectionHandler.checkDisposed();
            connection = schemaId == null ?
                    connectionHandler.getPoolConnection(readonly) :
                    connectionHandler.getPoolConnection(schemaId, readonly);

            connectionHandler.checkDisposed();
            connection.set(ResourceStatus.ACTIVE, true);
            runnable.run(connection);

        } catch (ProcessCanceledException ignore){
        } finally {
            if (connection != null) {
                connectionHandler.freePoolConnection(connection);
                connection.set(ResourceStatus.ACTIVE, false);
            }
        }
    }

    public static <R> R call(
            boolean readonly,
            @NotNull ConnectionHandler connectionHandler,
            @NotNull ParametricCallable<DBNConnection, R, SQLException> callable) throws SQLException{
        return call(readonly, connectionHandler, null, callable);
    }

    public static <R> R call(
            boolean readonly,
            @NotNull ConnectionHandler connectionHandler,
            @Nullable SchemaId schemaId,
            @NotNull ParametricCallable<DBNConnection, R, SQLException> callable) throws SQLException{

        DBNConnection connection = null;
        try {
            connectionHandler.checkDisposed();
            connection = schemaId == null ?
                    connectionHandler.getPoolConnection(readonly) :
                    connectionHandler.getPoolConnection(schemaId, readonly);

            connectionHandler.checkDisposed();
            connection.set(ResourceStatus.ACTIVE, true);
            return callable.call(connection);

        } finally {
            if (connection != null) {
                connectionHandler.freePoolConnection(connection);
                connection.set(ResourceStatus.ACTIVE, false);
            }
        }
    }
}
