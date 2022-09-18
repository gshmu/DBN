package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResource;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.database.DatabaseFeature;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.dci.intellij.dbn.common.util.Commons.nvl;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.isDatabaseResourceDebug;

@Slf4j
public final class Resources {
    private Resources() {}

    public static boolean isClosed(ResultSet resultSet) throws SQLException {
        try {
            return resultSet.isClosed();
        } catch (AbstractMethodError e) {
            // sqlite AbstractMethodError for osx
            return false;
        }
    }
    public static void cancel(DBNStatement statement) {
        if (statement != null && !statement.isClosed()) {
            try {
                invokeResourceAction(
                        statement,
                        ResourceStatus.CANCELLING,
                        () -> statement.cancel(),
                        () -> "[DBN] Cancelling " + statement,
                        () -> "[DBN] Done cancelling " + statement,
                        () -> "[DBN] Failed to cancel " + statement);
            } catch (Throwable ignore) {
            } finally {
                close((DBNResource) statement);
            }
        }
    }

    public static <T extends AutoCloseable> void close(T resource) {
        if (resource != null) {
            if (resource instanceof DBNResource) {
                close((DBNResource) resource);
            } else {
                try {
                    invokeResourceAction(
                            () -> resource.close(),
                            () -> "[DBN] Closing " + resource,
                            () -> "[DBN] Done closing " + resource,
                            () -> "[DBN] Failed to close " + resource);
                } catch (Throwable ignore) {}
            }
        }
    }

    private static <T extends DBNResource> void close(T resource) {
        if (resource != null && !resource.isClosed()) {
            try {
                invokeResourceAction(
                        resource,
                        ResourceStatus.CLOSING,
                        () -> resource.close(),
                        () -> "[DBN] Closing " + resource,
                        () -> "[DBN] Done closing " + resource,
                        () -> "[DBN] Failed to close " + resource);
            } catch (Throwable ignore) {}
        }
    }

    public static <T extends AutoCloseable> void close(Collection<T> resources) {
        for (T resource : resources) {
            close(resource);
        }
    }

    public static void commitSilently(DBNConnection connection) {
        Unsafe.silent(() -> commit(connection));
    }

    public static void commit(DBNConnection connection) throws SQLException {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
                invokeResourceAction(
                        connection,
                        ResourceStatus.COMMITTING,
                        () -> connection.commit(),
                        () -> "[DBN] Committing " + connection,
                        () -> "[DBN] Done committing " + connection,
                        () -> "[DBN] Failed to commit " + connection);
            }
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    NotificationGroup.TRANSACTION,
                    "Failed to commit",
                    connection,
                    e);
            throw e;
        }
    }

    public static void rollbackSilently(DBNConnection connection) {
        Unsafe.silent(() -> rollback(connection));
    }

    public static void rollback(DBNConnection connection) throws SQLException {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
                invokeResourceAction(
                        connection,
                        ResourceStatus.ROLLING_BACK,
                        () -> connection.rollback(),
                        () -> "[DBN] Rolling-back " + connection,
                        () -> "[DBN] Done rolling-back " + connection,
                        () -> "[DBN] Failed to roll-back " + connection);
            }
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    NotificationGroup.TRANSACTION,
                    "Failed to rollback",
                    connection,
                    e);
            throw e;
        }
    }

    public static void rollbackSilently(DBNConnection connection, @Nullable Savepoint savepoint) {
        Unsafe.silent(() -> rollback(connection, savepoint));
    }

    public static void rollback(DBNConnection connection, @Nullable Savepoint savepoint) throws SQLException {
        try {
            if (connection != null && savepoint != null && !connection.isClosed() && !connection.getAutoCommit()) {
                String savepointId = getSavepointIdentifier(savepoint);
                invokeResourceAction(
                        connection,
                        ResourceStatus.ROLLING_BACK_SAVEPOINT,
                        () -> connection.rollback(savepoint),
                        () -> "[DBN] Rolling-back savepoint '" + savepointId + "' on " + connection,
                        () -> "[DBN] Done rolling-back savepoint '" + savepointId + "' on " + connection,
                        () -> "[DBN] Failed to roll-back savepoint '" + savepointId + "' on " + connection);
            }
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    NotificationGroup.TRANSACTION,
                    "Failed to rollback savepoint for",
                    connection,
                    e);
            throw e;
        }
    }

    public static @Nullable Savepoint createSavepoint(DBNConnection connection) {
        try {
            if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
                AtomicReference<Savepoint> savepoint = new AtomicReference<>();
                invokeResourceAction(
                        connection,
                        ResourceStatus.CREATING_SAVEPOINT,
                        () -> savepoint.set(connection.setSavepoint()),
                        () -> "[DBN] Creating savepoint on " + connection,
                        () -> "[DBN] Done creating savepoint on " + connection,
                        () -> "[DBN] Failed to create savepoint on " + connection);
                return savepoint.get();
            }
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    NotificationGroup.TRANSACTION,
                    "Failed to create savepoint for",
                    connection,
                    e);
        }
        return null;
    }

    public static void releaseSavepoint(DBNConnection connection, @Nullable Savepoint savepoint) {
        try {
            if (connection != null && savepoint != null && !connection.isClosed() && !connection.getAutoCommit()) {
                String savepointId = getSavepointIdentifier(savepoint);
                invokeResourceAction(
                        connection,
                        ResourceStatus.RELEASING_SAVEPOINT,
                        () -> connection.releaseSavepoint(savepoint),
                        () -> "[DBN] Releasing savepoint '" + savepointId + "' on " + connection,
                        () -> "[DBN] Done releasing savepoint '" + savepointId + "' on " + connection,
                        () -> "[DBN] Failed to release savepoint '" + savepointId + "' on " + connection);
            }
        } catch (SQLRecoverableException ignore) {
        } catch (SQLException e) {
            sentWarningNotification(
                    NotificationGroup.TRANSACTION,
                    "Failed to release savepoint for",
                    connection,
                    e);
        }
    }

    public static void setReadonly(ConnectionHandler connection, DBNConnection conn, boolean readonly) {
        boolean readonlySupported = DatabaseFeature.READONLY_CONNECTIVITY.isSupported(connection);
        if (readonlySupported) {
            try {
                invokeResourceAction(
                        conn,
                        ResourceStatus.CHANGING_READ_ONLY,
                        () -> conn.setReadOnly(readonly),
                        () -> "[DBN] Applying status READ_ONLY=" + readonly + " on " + conn,
                        () -> "[DBN] Done applying status READ_ONLY=" + readonly + " on " + conn,
                        () -> "[DBN] Failed to apply status READ_ONLY=" + readonly + " on " + conn);
            } catch (SQLRecoverableException ignore) {
            } catch (SQLException e) {
                sentWarningNotification(
                        NotificationGroup.CONNECTION,
                        "Failed to initialize readonly status for",
                        conn,
                        e);
            }
        }
    }

    public static void setAutoCommit(DBNConnection connection, boolean autoCommit) {
        try {
            if (connection != null && !connection.isClosed()) {
                invokeResourceAction(
                        connection,
                        ResourceStatus.CHANGING_AUTO_COMMIT, () -> connection.setAutoCommit(autoCommit),
                        () -> "[DBN] Applying status AUTO_COMMIT=" + autoCommit + " on " + connection,
                        () -> "[DBN] Done applying status AUTO_COMMIT=" + autoCommit + " on " + connection,
                        () -> "[DBN] Failed to apply status AUTO_COMMIT=" + autoCommit + " on " + connection);

                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLRecoverableException ignore) {
        } catch (Exception e) {
            sentWarningNotification(
                    NotificationGroup.CONNECTION,
                    "Failed to change auto-commit status for",
                    connection,
                    e);
        }
    }

    private static void sentWarningNotification(NotificationGroup title, String message, DBNConnection connection, Exception e) {
        String error = nvl(e.getMessage(), e.getClass().getName());
        if (connection.shouldNotify(error)) {
            String name = connection.getName();
            SessionId sessionId = connection.getSessionId();
            String errorMessage = e.getMessage();
            String notificationMessage = message + " connection \"" + name + " (" + sessionId + ")\": " + errorMessage;

            NotificationSupport.sendWarningNotification(
                    connection.getProject(),
                    title,
                    notificationMessage);
        }
    }

    private static <E extends Throwable> void invokeResourceAction(
            @NotNull DBNResource<?> resource,
            @NotNull ResourceStatus transientStatus,
            @NotNull ThrowableRunnable<E> action,
            @NotNull Supplier<String> startMessage,
            @NotNull Supplier<String> successMessage,
            @NotNull Supplier<String> errorMessage) throws E{

        if (!resource.is(transientStatus)) {
            try {
                resource.set(transientStatus, true);
                invokeResourceAction(action, startMessage, successMessage, errorMessage);
            } finally {
                resource.set(transientStatus, false);
            }
        }
    }

    private static <E extends Throwable> void invokeResourceAction(
            @NotNull ThrowableRunnable<E> action,
            @NotNull Supplier<String> startMessage,
            @NotNull Supplier<String> successMessage,
            @NotNull Supplier<String> errorMessage) throws E{

        long start = System.currentTimeMillis();
        if (isDatabaseResourceDebug()) log.info(startMessage.get() + "...");
        try {
            action.run();
            if (isDatabaseResourceDebug()) log.info(successMessage.get() + " - " + (System.currentTimeMillis() - start) + "ms");
        } catch (Throwable t) {
            log.warn(errorMessage.get() + " Cause: " + t.getMessage());
            throw t;
        }
    }

    public static String getSavepointIdentifier(Savepoint savepoint) {
        try {
            return savepoint.getSavepointName();
        } catch (SQLException e) {
            try {
                return Integer.toString(savepoint.getSavepointId());
            } catch (SQLException ex) {
                return "UNKNOWN";
            }
        }
    }
}
