package com.dci.intellij.dbn.connection.transaction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.dci.intellij.dbn.connection.transaction.ui.UncommittedChangesDialog;
import com.dci.intellij.dbn.connection.transaction.ui.UncommittedChangesOverviewDialog;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseTransactionManager extends AbstractProjectComponent implements ProjectManagerListener{

    private DatabaseTransactionManager(Project project) {
        super(project);
    }

    public static DatabaseTransactionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseTransactionManager.class);
    }

    public void execute(final ConnectionHandler connectionHandler, boolean background, final TransactionAction... actions) {
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
            String connectionName = connectionHandler.getName();
            if (ApplicationManager.getApplication().isDisposeInProgress()) {
                executeActions(connectionHandler, actionList);
            } else {
                new BackgroundTask(project, "Performing \"" + actionList.get(0).getName() + "\" on connection " + connectionName, background) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator indicator) {
                        executeActions(connectionHandler, actionList);
                    }
                }.start();
            }
        }
    }

    public TransactionManagerSettings getTransactionManagerSettings() {
        return ProjectSettingsManager.getInstance(getProject()).getOperationSettings().getTransactionManagerSettings();
    }

    private void executeActions(ConnectionHandler connectionHandler, List<TransactionAction> actions) {
        Project project = getProject();
        String connectionName = connectionHandler.getName();
        TransactionListener transactionListener = EventUtil.notify(project, TransactionListener.TOPIC);
        for (TransactionAction action : actions) {
            executeAction(connectionHandler, project, connectionName, transactionListener, action);
        }
    }

    private void executeAction(ConnectionHandler connectionHandler, Project project, String connectionName, TransactionListener transactionListener, TransactionAction action) {
        boolean success = true;
        try {
            // notify pre-action
            transactionListener.beforeAction(connectionHandler, action);
            ProgressMonitor.setTaskDescription("Performing " + action.getName() + " on connection " + connectionName);

            action.execute(connectionHandler);
            if (action.getNotificationType() != null) {
                NotificationUtil.sendNotification(
                        project,
                        action.getNotificationType(),
                        Constants.DBN_TITLE_PREFIX + action.getName(),
                        action.getSuccessNotificationMessage(),
                        connectionName);
            }
        } catch (SQLException ex) {
            NotificationUtil.sendNotification(
                    project,
                    action.getFailureNotificationType(),
                    Constants.DBN_TITLE_PREFIX + action.getName(),
                    action.getFailureNotificationMessage(),
                    connectionName,
                    ex.getMessage());
            success = false;
        } finally {
            if (action != null && !project.isDisposed()) {
                // notify post-action
                transactionListener.afterAction(connectionHandler, action, success);

                if (action.isStatusChange()) {
                    ConnectionStatusListener statusListener = EventUtil.notify(project, ConnectionStatusListener.TOPIC);
                    statusListener.statusChanged(connectionHandler.getId());
                }
                connectionHandler.getPendingActions().remove(action);
            }
        }
    }

    public void commit(final @NotNull ConnectionHandler connectionHandler, boolean fromEditor, boolean background) {
        if (fromEditor && connectionHandler.getUncommittedChanges().size() > 1) {
            Project project = connectionHandler.getProject();
            VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
            if (selectedFile != null) {
                InteractiveOptionHandler<TransactionOption> commitMultipleChanges = getTransactionManagerSettings().getCommitMultipleChanges();
                TransactionOption result = commitMultipleChanges.resolve(connectionHandler.getName(), selectedFile.getPresentableUrl());
                switch (result) {
                    case COMMIT: execute(connectionHandler, background, TransactionAction.COMMIT); break;
                    case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, null); break;
                }
            }
        } else {
            execute(connectionHandler, background, TransactionAction.COMMIT);
        }
    }

    public void rollback(final @NotNull ConnectionHandler connectionHandler, boolean fromEditor, boolean background) {
        if (fromEditor && connectionHandler.getUncommittedChanges().size() > 1) {
            Project project = connectionHandler.getProject();
            VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
            if (selectedFile != null) {
                InteractiveOptionHandler<TransactionOption> rollbackMultipleChanges = getTransactionManagerSettings().getRollbackMultipleChanges();
                TransactionOption result = rollbackMultipleChanges.resolve(connectionHandler.getName(), selectedFile.getPresentableUrl());
                switch (result) {
                    case ROLLBACK: execute(connectionHandler, background, TransactionAction.ROLLBACK); break;
                    case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, null); break;
                }
            }
        } else {
            execute(connectionHandler, background, TransactionAction.ROLLBACK);
        }
    }

    public void disconnect(final ConnectionHandler connectionHandler, boolean background) {
        execute(connectionHandler, background, TransactionAction.DISCONNECT);
    }


    public boolean showUncommittedChangesOverviewDialog(@Nullable TransactionAction additionalOperation) {
        UncommittedChangesOverviewDialog executionDialog = new UncommittedChangesOverviewDialog(getProject(), additionalOperation);
        executionDialog.show();
        return executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
    }

    public boolean showUncommittedChangesDialog(ConnectionHandler connectionHandler, @Nullable TransactionAction additionalOperation) {
        UncommittedChangesDialog executionDialog = new UncommittedChangesDialog(connectionHandler, additionalOperation, false);
        executionDialog.show();
        return executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
    }

    public void toggleAutoCommit(ConnectionHandler connectionHandler) {
        boolean isAutoCommit = connectionHandler.isAutoCommit();
        TransactionAction autoCommitAction = isAutoCommit ?
                TransactionAction.TURN_AUTO_COMMIT_OFF :
                TransactionAction.TURN_AUTO_COMMIT_ON;

        if (!isAutoCommit && connectionHandler.hasUncommittedChanges()) {
            InteractiveOptionHandler<TransactionOption> toggleAutoCommit = getTransactionManagerSettings().getToggleAutoCommit();
            TransactionOption result = toggleAutoCommit.resolve(connectionHandler.getName());
            switch (result) {
                case COMMIT: execute(connectionHandler, true, TransactionAction.COMMIT, autoCommitAction); break;
                case ROLLBACK: execute(connectionHandler, true, TransactionAction.ROLLBACK, autoCommitAction); break;
                case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, autoCommitAction);
            }
        } else {
            execute(connectionHandler, false, autoCommitAction);
        }
    }

    public void disconnect(ConnectionHandler connectionHandler) {
        if (connectionHandler.hasUncommittedChanges()) {
            InteractiveOptionHandler<TransactionOption> disconnect = getTransactionManagerSettings().getDisconnect();
            TransactionOption result = disconnect.resolve(connectionHandler.getName());

            switch (result) {
                case COMMIT: execute(connectionHandler, false, TransactionAction.COMMIT, TransactionAction.DISCONNECT); break;
                case ROLLBACK: execute(connectionHandler, false, TransactionAction.DISCONNECT); break;
                case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, TransactionAction.DISCONNECT);
            }
        } else {
            execute(connectionHandler, false, TransactionAction.DISCONNECT);
        }
    }

    /**********************************************
    *            ProjectManagerListener           *
    ***********************************************/

    /**********************************************
    *                ProjectComponent             *
    ***********************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.TransactionManager";
    }
}