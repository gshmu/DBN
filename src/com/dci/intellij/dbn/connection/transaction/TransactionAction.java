package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.common.constant.Constant;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.intellij.notification.NotificationType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TransactionAction implements Serializable, Constant<TransactionAction> {
    COMMIT(
            "Commit",
            NotificationGroup.TRANSACTION,
            NotificationType.INFORMATION, "Connection \"{0}\" committed",
            NotificationType.ERROR, "Error committing connection \"{0}\". Details: {1}",
            false,
            (connectionHandler, connection) -> ResourceUtil.commit(connection)),

    ROLLBACK(
            "Rollback",
            NotificationGroup.TRANSACTION,
            NotificationType.INFORMATION, "Connection \"{0}\" rolled back.",
            NotificationType.ERROR, "Error rolling back connection \"{0}\". Details: {1}",
            false,
            (connectionHandler, connection) -> ResourceUtil.rollback(connection)),

    ROLLBACK_IDLE(
            "Idle Rollback",
            NotificationGroup.TRANSACTION,
            NotificationType.INFORMATION, "Connection \"{0}\" rolled back.",
            NotificationType.ERROR, "Error rolling back connection \"{0}\". Details: {1}",
            false,
            (connectionHandler, connection) -> ResourceUtil.rollback(connection)),

    DISCONNECT(
            "Disconnect",
            NotificationGroup.SESSION,
            NotificationType.INFORMATION, "Disconnected from \"{0}\"",
            NotificationType.WARNING, "Error disconnecting from \"{0}\". Details: {1}",
            true,
            (connectionHandler, connection) -> connectionHandler.closeConnection(connection)),

    DISCONNECT_IDLE(
            "Idle disconnect",
            NotificationGroup.SESSION,
            NotificationType.INFORMATION, "Disconnected from \"{0}\" - exceeded the configured idle timeout.",
            NotificationType.WARNING, "Error disconnecting from \"{0}\". Details: {1}",
            true,
            (connectionHandler, connection) -> connectionHandler.closeConnection(connection)),

    KEEP_ALIVE(
            "Keep Alive",
            NotificationGroup.CONNECTION,
            null, "",
            NotificationType.ERROR, "Error checking connectivity for \"{0}\". Details: {1}",
            false,
            (connectionHandler, connection) -> connection.updateLastAccess()),

    TURN_AUTO_COMMIT_ON(
            "Enable Auto-Commit",
            NotificationGroup.TRANSACTION,
            NotificationType.WARNING, "Auto-Commit switched ON for connection \"{0}\".",
            NotificationType.ERROR, "Error switching Auto-Commit ON for connection \"{0}\". Details: {1}",
            true,
            (connectionHandler, connection) -> connection.setAutoCommit(true)),

    TURN_AUTO_COMMIT_OFF(
            "Disable Auto-Commit",
            NotificationGroup.TRANSACTION,
            NotificationType.INFORMATION, "Auto-Commit switched OFF for connection \"{0}\".",
            NotificationType.ERROR, "Error switching Auto-Commit OFF for connection\"{0}\". Details: {1}",
            true,
            (connectionHandler, connection) -> connection.setAutoCommit(false));


    private final @Getter NotificationGroup group;
    private final @Getter String name;
    private final @Getter String successNotificationMessage;
    private final @Getter String failureNotificationMessage;
    private final @Getter NotificationType notificationType;
    private final @Getter NotificationType failureNotificationType;
    private final @Getter Executor executor;
    private final @Getter boolean statusChange;

    TransactionAction(String name, NotificationGroup group, NotificationType notificationType, String successNotificationMessage, NotificationType failureNotificationType, String failureNotificationMessage, boolean statusChange, Executor executor) {
        this.group = group;
        this.name = name;
        this.failureNotificationMessage = failureNotificationMessage;
        this.successNotificationMessage = successNotificationMessage;
        this.executor = executor;
        this.statusChange = statusChange;
        this.notificationType = notificationType;
        this.failureNotificationType = failureNotificationType;
    }

    @FunctionalInterface
    private interface Executor {
        void execute(@NotNull ConnectionHandler connectionHandler, @NotNull DBNConnection connection) throws SQLException;
    }

    public void execute(@NotNull ConnectionHandler connectionHandler, @NotNull DBNConnection connection) throws SQLException {
        executor.execute(connectionHandler, connection);
    }

    public static List<TransactionAction> actions(TransactionAction ... actions) {
        return Arrays.stream(actions).filter(action -> action != null).collect(Collectors.toList());
    }

}
