package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.resource.ui.ResourceMonitorDialog;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.dci.intellij.dbn.connection.transaction.ui.PendingTransactionsDetailDialog;
import com.dci.intellij.dbn.connection.transaction.ui.PendingTransactionsDialog;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DatabaseTransactionManager extends AbstractProjectComponent implements ProjectManagerListener{

    private DatabaseTransactionManager(Project project) {
        super(project);
    }

    public static DatabaseTransactionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseTransactionManager.class);
    }

    public void execute(final ConnectionHandler connectionHandler, final DBNConnection connection, boolean background, final TransactionAction... actions) {
        if (connection == null) {
            List<DBNConnection> activeConnections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
            for (DBNConnection activeConnection : activeConnections) {
                execute(connectionHandler, activeConnection, background, actions);
            }
        } else {
            final List<TransactionAction> actionList = new ArrayList<TransactionAction>();
            for (TransactionAction action : actions) {
                if (action != null) {
                    actionList.add(action);
                }
            }

            Set<TransactionAction> pendingActions = connectionHandler.getPendingActions();

            actionList.removeAll(pendingActions);
            if (actionList.size() > 0) {
                pendingActions.addAll(actionList);
                Project project = connectionHandler.getProject();
                if (ApplicationManager.getApplication().isDisposeInProgress()) {
                    executeActions(connectionHandler, connection, actionList);
                } else {
                    String connectionName = connectionHandler.getConnectionName(connection);
                    String taskTitle = "Performing \"" + actionList.get(0).getName() + "\" on connection " + connectionName;
                    BackgroundTask.invoke(project, taskTitle, background, false, (task, progress) -> {
                        executeActions(connectionHandler, connection, actionList);
                    });
                }
            }
        }
    }

    public TransactionManagerSettings getTransactionManagerSettings() {
        return ProjectSettingsManager.getInstance(getProject()).getOperationSettings().getTransactionManagerSettings();
    }

    private void executeActions(ConnectionHandler connectionHandler, DBNConnection connection, List<TransactionAction> actions) {
        Project project = getProject();
        TransactionListener transactionListener = EventUtil.notify(project, TransactionListener.TOPIC);
        for (TransactionAction action : actions) {
            executeAction(connectionHandler, connection, project, transactionListener, action);
        }
    }

    private void executeAction(ConnectionHandler connectionHandler, DBNConnection connection, Project project, TransactionListener transactionListener, TransactionAction action) {
        String connectionName = connectionHandler.getConnectionName(connection);
        boolean success = true;
        try {
            // notify pre-action
            transactionListener.beforeAction(connectionHandler, connection, action);
            ProgressMonitor.setTaskDescription("Performing " + action.getName() + " on connection " + connectionName);

            action.execute(connectionHandler, connection);
            if (action.getNotificationType() != null) {
                NotificationUtil.sendNotification(
                        project,
                        action.getNotificationType(),
                        Constants.DBN_TITLE_PREFIX + action.getGroup(),
                        action.getSuccessNotificationMessage(),
                        connectionName);
            }
        } catch (SQLException ex) {
            NotificationUtil.sendNotification(
                    project,
                    action.getFailureNotificationType(),
                    Constants.DBN_TITLE_PREFIX + action.getGroup(),
                    action.getFailureNotificationMessage(),
                    connectionName,
                    ex.getMessage());
            success = false;
        } finally {
            if (action != null && !project.isDisposed()) {
                // notify post-action
                transactionListener.afterAction(connectionHandler, connection, action, success);

                if (action.isStatusChange()) {
                    ConnectionHandlerStatusListener statusListener = EventUtil.notify(project, ConnectionHandlerStatusListener.TOPIC);
                    statusListener.statusChanged(connectionHandler.getId(), ConnectionHandlerStatus.BUSY);
                }
                connectionHandler.getPendingActions().remove(action);
            }
        }
    }

    public void commit(final @NotNull ConnectionHandler connectionHandler, @Nullable DBNConnection targetConnection, boolean fromEditor, boolean background) {
        List<DBNConnection> connections;
        if (targetConnection == null) {
            connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
        } else {
            connections = new ArrayList<DBNConnection>();
            connections.add(targetConnection);
        }
        for (DBNConnection connection : connections) {
            PendingTransactionBundle dataChanges = connection.getDataChanges();
            if (fromEditor && dataChanges != null && dataChanges.size() > 1) {
                Project project = connectionHandler.getProject();
                VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
                if (selectedFile != null) {
                    String connectionName = connectionHandler.getConnectionName(connection);

                    InteractiveOptionHandler<TransactionOption> commitMultipleChanges = getTransactionManagerSettings().getCommitMultipleChanges();
                    TransactionOption result = commitMultipleChanges.resolve(connectionName, selectedFile.getPresentableUrl());
                    switch (result) {
                        case COMMIT: execute(connectionHandler, connection, background, TransactionAction.COMMIT); break;
                        case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, null); break;
                    }
                }
            } else {
                execute(connectionHandler, connection, background, TransactionAction.COMMIT);
            }
        }

    }

    public void rollback(final @NotNull ConnectionHandler connectionHandler, @Nullable DBNConnection targetConnection, boolean fromEditor, boolean background) {
        List<DBNConnection> connections;
        if (targetConnection == null) {
            connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
        } else {
            connections = new ArrayList<DBNConnection>();
            connections.add(targetConnection);
        }

        for (DBNConnection connection : connections) {
            PendingTransactionBundle dataChanges = connection.getDataChanges();
            if (fromEditor && dataChanges != null && dataChanges.size() > 1) {
                Project project = connectionHandler.getProject();
                VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
                if (selectedFile != null) {
                    String connectionName = connectionHandler.getConnectionName(connection);

                    InteractiveOptionHandler<TransactionOption> rollbackMultipleChanges = getTransactionManagerSettings().getRollbackMultipleChanges();
                    TransactionOption result = rollbackMultipleChanges.resolve(connectionName, selectedFile.getPresentableUrl());
                    switch (result) {
                        case ROLLBACK: execute(connectionHandler, connection, background, TransactionAction.ROLLBACK); break;
                        case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, null); break;
                    }
                }
            } else {
                execute(connectionHandler, connection, background, TransactionAction.ROLLBACK);
            }
        }
    }

    public void disconnect(final ConnectionHandler connectionHandler, boolean background) {
        List<DBNConnection> connections = connectionHandler.getConnections();
        for (DBNConnection connection : connections) {
            execute(connectionHandler, connection, background, TransactionAction.DISCONNECT);
        }
    }


    public void showResourceMonitorDialog() {
        ResourceMonitorDialog resourceMonitorDialog = new ResourceMonitorDialog(getProject());
        resourceMonitorDialog.show();
    }

    public boolean showPendingTransactionsOverviewDialog(@Nullable TransactionAction additionalOperation) {
        PendingTransactionsDialog executionDialog = new PendingTransactionsDialog(getProject(), additionalOperation);
        executionDialog.show();
        return executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
    }

    public boolean showPendingTransactionsDialog(ConnectionHandler connectionHandler, @Nullable TransactionAction additionalOperation) {
        PendingTransactionsDetailDialog executionDialog = new PendingTransactionsDetailDialog(connectionHandler, additionalOperation, false);
        executionDialog.show();
        return executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
    }

    public void toggleAutoCommit(ConnectionHandler connectionHandler) {
        boolean autoCommit = connectionHandler.isAutoCommit();
        TransactionAction autoCommitAction = autoCommit ?
                TransactionAction.TURN_AUTO_COMMIT_OFF :
                TransactionAction.TURN_AUTO_COMMIT_ON;

        List<DBNConnection> connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
        if (connections.size() == 0) {
            connectionHandler.setAutoCommit(!autoCommit);
        } else {
            for (DBNConnection connection : connections) {
                if (!autoCommit && connection.hasDataChanges()) {
                    String connectionName = connectionHandler.getConnectionName(connection);

                    InteractiveOptionHandler<TransactionOption> toggleAutoCommit = getTransactionManagerSettings().getToggleAutoCommit();
                    TransactionOption result = toggleAutoCommit.resolve(connectionName);
                    switch (result) {
                        case COMMIT: execute(connectionHandler, connection, true, TransactionAction.COMMIT, autoCommitAction); break;
                        case ROLLBACK: execute(connectionHandler, connection, true, TransactionAction.ROLLBACK, autoCommitAction); break;
                        case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, autoCommitAction);
                    }
                } else {
                    execute(connectionHandler, connection, false, autoCommitAction);
                }
            }
        }
    }

    public void disconnect(ConnectionHandler connectionHandler) {
        List<DBNConnection> connections = connectionHandler.getConnections();
        for (DBNConnection connection : connections) {
            if (connection.hasDataChanges()) {
                InteractiveOptionHandler<TransactionOption> disconnect = getTransactionManagerSettings().getDisconnect();
                TransactionOption result = disconnect.resolve(connectionHandler.getName());

                switch (result) {
                    case COMMIT: execute(connectionHandler, connection, false, TransactionAction.COMMIT, TransactionAction.DISCONNECT); break;
                    case ROLLBACK: execute(connectionHandler, connection, false, TransactionAction.DISCONNECT); break;
                    case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, TransactionAction.DISCONNECT);
                }
            } else {
                execute(connectionHandler, connection, false, TransactionAction.DISCONNECT);
            }
        }
    }

   /**********************************************
    *                ProjectComponent             *
    ***********************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.TransactionManager";
    }
}