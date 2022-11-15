package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionCallable;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface.ConnectionRunnable;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceContext;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class PooledConnection {
    private PooledConnection() {}

    public static void run(@NotNull DatabaseInterfaceContext context, @NotNull ConnectionRunnable runnable) throws SQLException {
        ConnectionHandler connection = context.getConnection();
        SchemaId schemaId = context.getSchemaId();
        boolean readonly = context.isReadonly();

        DBNConnection conn = null;
        try {
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

    public static <T> T call(@NotNull DatabaseInterfaceContext context, @NotNull ConnectionCallable<T> callable) throws SQLException {
        ConnectionHandler connection = context.getConnection();
        SchemaId schemaId = context.getSchemaId();
        boolean readonly = context.isReadonly();

        DBNConnection c = null;
        try {
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
