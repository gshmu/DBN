package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface PooledConnection {
    static void run(
            boolean readonly,
            @NotNull ConnectionHandler connectionHandler,
            @NotNull ParametricRunnable<DBNConnection, SQLException> runnable) throws SQLException {

        DBNConnection connection = null;
        try {
            connection = connectionHandler.getPoolConnection(readonly);
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

    static <R> R call(
            boolean readonly,
            @NotNull ConnectionHandler connectionHandler,
            @NotNull ParametricCallable<DBNConnection, R, SQLException> callable) throws SQLException{

        DBNConnection connection = null;
        try {
            connection = connectionHandler.getPoolConnection(readonly);
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
