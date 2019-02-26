package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.routine.ProgressRunnable;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
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
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.util.CollectionUtil.isLast;
import static com.dci.intellij.dbn.common.util.CommonUtil.list;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.COMMIT;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.DISCONNECT;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.ROLLBACK;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.TURN_AUTO_COMMIT_OFF;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.TURN_AUTO_COMMIT_ON;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.actions;

public class DatabaseTransactionManager extends AbstractProjectComponent implements ProjectManagerListener{

    private DatabaseTransactionManager(Project project) {
        super(project);
    }

    public static DatabaseTransactionManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseTransactionManager.class);
    }

    public void execute(
            @NotNull ConnectionHandler connectionHandler,
            @Nullable DBNConnection connection,
            @NotNull List<TransactionAction> actions,
            boolean background,
            @Nullable Runnable callback) {

        if (connection == null) {
            List<DBNConnection> connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
            for (DBNConnection conn : connections) {
                Runnable executionCallback = isLast(connections, conn) ? callback : null;
                execute(connectionHandler, conn, actions, background, executionCallback);
            }
        } else {
            Project project = connectionHandler.getProject();
            if (ApplicationManager.getApplication().isDisposeInProgress()) {
                executeActions(connectionHandler, connection, actions, callback);
            } else {
                String connectionName = connectionHandler.getConnectionName(connection);
                String actionName = actions.get(0).getName();

                String title = "Performing \"" + actionName + "\" on connection " + connectionName;
                ProgressRunnable executor = (progress) -> executeActions(connectionHandler, connection, actions, callback);

                if (background)
                    Progress.background(project, title, false, executor); else
                    Progress.prompt(project, title, false, executor);
            }
        }
    }



    private void executeActions(
            @NotNull ConnectionHandler connectionHandler,
            @NotNull DBNConnection connection,
            @NotNull List<TransactionAction> actions,
            @Nullable Runnable callback) {

        Project project = getProject();
        TransactionListener transactionListener = EventUtil.notify(project, TransactionListener.TOPIC);
        for (TransactionAction action : actions) {
            Runnable actionCallback = isLast(actions, action) ? callback : null;
            executeAction(connectionHandler, connection, project, transactionListener, action);
        }
        Failsafe.run(callback);
    }

    private void executeAction(
            @NotNull ConnectionHandler connectionHandler,
            @NotNull DBNConnection connection,
            @NotNull Project project,
            @NotNull TransactionListener transactionListener,
            @NotNull TransactionAction action) {

        String connectionName = connectionHandler.getConnectionName(connection);
        boolean success = true;
        try {
            // notify pre-action
            transactionListener.beforeAction(connectionHandler, connection, action);
            ProgressMonitor.setTaskDescription("Performing " + action.getName() + " on connection " + connectionName);

            action.execute(connectionHandler, connection);
            if (action.getNotificationType() != null) {
                sendNotification(
                        action.getNotificationType(),
                        Constants.DBN_TITLE_PREFIX + action.getGroup(),
                        action.getSuccessNotificationMessage(),
                        connectionName);
            }
        } catch (SQLException ex) {
            sendNotification(
                    action.getFailureNotificationType(),
                    Constants.DBN_TITLE_PREFIX + action.getGroup(),
                    action.getFailureNotificationMessage(),
                    connectionName,
                    ex.getMessage());
            success = false;
        } finally {
            if (!project.isDisposed()) {
                // notify post-action
                transactionListener.afterAction(connectionHandler, connection, action, success);

                if (action.isStatusChange()) {
                    ConnectionHandlerStatusListener statusListener = EventUtil.notify(project, ConnectionHandlerStatusListener.TOPIC);
                    statusListener.statusChanged(connectionHandler.getConnectionId());
                }
            }
        }
    }

    public void commit(
            @NotNull ConnectionHandler connectionHandler,
            @Nullable DBNConnection connection,
            boolean fromEditor,
            boolean background,
            @Nullable Runnable callback) {

        List<TransactionAction> actions = actions(COMMIT);

        List<DBNConnection> connections = connection == null ?
                connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION) :
                Collections.singletonList(connection);

        for (DBNConnection conn : connections) {
            Runnable commitCallback = isLast(connections, conn) ? callback : null;

            PendingTransactionBundle dataChanges = conn.getDataChanges();
            if (fromEditor && dataChanges != null && dataChanges.size() > 1) {
                Project project = connectionHandler.getProject();
                VirtualFile selectedFile = EditorUtil.getSelectedFile(project);

                if (selectedFile != null) {
                    String connectionName = connectionHandler.getConnectionName(conn);
                    String fileUrl = selectedFile.getPresentableUrl();

                    getSettings().getCommitMultipleChanges().resolve(
                            list(connectionName, fileUrl),
                            option -> {
                                switch (option) {
                                    case COMMIT: execute(connectionHandler, conn, actions, background, commitCallback); break;
                                    case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, null); break;
                                }
                            });
                }
            } else {
                execute(connectionHandler, conn, actions, background, commitCallback);
            }
        }

    }

    public void rollback(
            @NotNull ConnectionHandler connectionHandler,
            @Nullable DBNConnection targetConnection,
            boolean fromEditor,
            boolean background,
            @Nullable Runnable callback) {

        List<TransactionAction> actions = actions(ROLLBACK);

        List<DBNConnection> connections = targetConnection == null ?
                connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION) :
                Collections.singletonList(targetConnection);

        for (DBNConnection connection : connections) {
            Runnable rollbackCallback = isLast(connections, connection) ? callback : null;

            PendingTransactionBundle dataChanges = connection.getDataChanges();
            if (fromEditor && dataChanges != null && dataChanges.size() > 1) {
                Project project = connectionHandler.getProject();
                VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
                if (selectedFile != null) {
                    String connectionName = connectionHandler.getConnectionName(connection);

                    getSettings().getRollbackMultipleChanges().resolve(
                            list(connectionName, selectedFile.getPresentableUrl()),
                            option -> {
                                switch (option) {
                                    case ROLLBACK: execute(connectionHandler, connection, actions, background, rollbackCallback); break;
                                    case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, null); break;
                                }
                            });
                }
            } else {
                execute(connectionHandler, connection, actions, background, rollbackCallback);
            }
        }
    }

    public void disconnect(ConnectionHandler connectionHandler, boolean background, @Nullable Runnable callback) {
        List<DBNConnection> connections = connectionHandler.getConnections();
        for (DBNConnection connection : connections) {
            Runnable disconnectCallback = isLast(connections, connection) ? callback : null;
            execute(connectionHandler, connection, actions(DISCONNECT), background, disconnectCallback);
        }
    }

    public TransactionManagerSettings getSettings() {
        ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(getProject());
        return projectSettingsManager.getOperationSettings().getTransactionManagerSettings();
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
                TURN_AUTO_COMMIT_OFF :
                TURN_AUTO_COMMIT_ON;

        List<DBNConnection> connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
        connectionHandler.setAutoCommit(!autoCommit);
        for (DBNConnection connection : connections) {
            if (!autoCommit && connection.hasDataChanges()) {
                String connectionName = connectionHandler.getConnectionName(connection);

                getSettings().getToggleAutoCommit().resolve(
                        list(connectionName),
                        option -> {
                            switch (option) {
                                case COMMIT:   execute(connectionHandler, connection, actions(COMMIT, autoCommitAction), true, null); break;
                                case ROLLBACK: execute(connectionHandler, connection, actions(ROLLBACK, autoCommitAction), true, null); break;
                                case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, autoCommitAction);
                            }});
            } else {
                execute(connectionHandler, connection, actions(autoCommitAction), false, null);
            }
        }
    }

    public void disconnect(ConnectionHandler connectionHandler) {
        List<DBNConnection> connections = connectionHandler.getConnections();
        for (DBNConnection connection : connections) {
            if (connection.hasDataChanges()) {
                String connectionName = connectionHandler.getConnectionName(connection);

                getSettings().getDisconnect().resolve(
                        list(connectionName),
                        option -> {
                            switch (option) {
                                case COMMIT:   execute(connectionHandler, connection, actions(COMMIT, DISCONNECT), false, null);break;
                                case ROLLBACK: execute(connectionHandler, connection, actions(DISCONNECT), false, null); break;
                                case REVIEW_CHANGES: showPendingTransactionsDialog(connectionHandler, DISCONNECT);
                            }
                        });
            } else {
                execute(connectionHandler, connection, actions(DISCONNECT), false, null);
            }
        }
    }

   /**********************************************
    *                ProjectComponent             *
    ***********************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.TransactionManager";
    }
}