package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionCallable;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionRunnable;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;

public final class PooledConnection {
    private PooledConnection() {}

    public static void run(@NotNull ConnectionContext context, @NotNull ConnectionRunnable runnable) throws SQLException {
        ConnectionHandler connection = context.getConnection();
        SchemaId schemaId = context.getSchemaId();

        DBNConnection conn = null;
        try {
            conn = schemaId == null ?
                    connection.getPoolConnection(true) :
                    connection.getPoolConnection(schemaId, true);

            connection.checkDisposed();
            conn.set(ResourceStatus.ACTIVE, true);
            runnable.run(conn);

        } catch (ProcessCanceledException e){
            conditionallyLog(e);
        } finally {
            if (conn != null) {
                connection.freePoolConnection(conn);
                conn.set(ResourceStatus.ACTIVE, false);
            }
        }
    }

    public static <T> T call(@NotNull ConnectionContext context, @NotNull ConnectionCallable<T> callable) throws SQLException {
        ConnectionHandler connection = context.getConnection();
        SchemaId schemaId = context.getSchemaId();

        DBNConnection c = null;
        try {
            c = schemaId == null ?
                    connection.getPoolConnection(true) :
                    connection.getPoolConnection(schemaId, true);

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
