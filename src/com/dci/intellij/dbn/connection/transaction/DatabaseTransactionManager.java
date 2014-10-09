package com.dci.intellij.dbn.connection.transaction;

import java.sql.SQLException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.transaction.ui.UncommittedChangesDialog;
import com.dci.intellij.dbn.connection.transaction.ui.UncommittedChangesOverviewDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseTransactionManager extends AbstractProjectComponent implements ProjectManagerListener{
    private InteractiveOptionHandler toggleAutoCommitOptionHandler = new InteractiveOptionHandler(
            "Uncommitted changes",
            "You have uncommitted changes on the connection \"{0}\". \n" +
            "Please specify whether to commit or rollback these changes before switching Auto-Commit ON.",
            2, "Commit", "Rollback", "Review Changes", "Cancel");

    private InteractiveOptionHandler disconnectOptionHandler = new InteractiveOptionHandler(
            "Uncommitted changes",
            "You have uncommitted changes on the connection \"{0}\". \n" +
            "Please specify whether to commit or rollback these changes before disconnecting",
            2, "Commit", "Rollback", "Review Changes", "Cancel");

    private InteractiveOptionHandler commitMultipleChangesOptionHandler = new InteractiveOptionHandler(
            "Commit multiple changes",
            "This commit action will affect several other changes on the connection \"{0}\", " +
                    "\nnot only the ones done in \"{1}\"",
            1, "Commit", "Review Changes", "Cancel");

    private InteractiveOptionHandler rollbackMultipleChangesOptionHandler = new InteractiveOptionHandler(
            "Rollback multiple changes",
            "This rollback action will affect several other changes on the connection \"{0}\", " +
                    "\nnot only the ones done in \"{1}\"",
            1, "Rollback", "Review Changes", "Cancel");


    private DatabaseTransactionManager(Project project) {
        super(project);
    }

    public static DatabaseTransactionManager getInstance(Project project) {
        return project.getComponent(DatabaseTransactionManager.class);
    }

    public void execute(final ConnectionHandler connectionHandler, boolean background, final TransactionAction... actions) {
        Project project = connectionHandler.getProject();
        String connectionName = connectionHandler.getName();
        if (ApplicationManager.getApplication().isDisposeInProgress()) {
            executeActions(connectionHandler, actions);
        } else {
            new BackgroundTask(project, "Performing " + actions[0].getName() + " on connection " + connectionName, background) {
                @Override
                public void execute(@NotNull ProgressIndicator indicator) {
                    executeActions(connectionHandler, actions);
                }
            }.start();
        }
    }

    public void executeActions(ConnectionHandler connectionHandler, TransactionAction... actions) {
        Project project = connectionHandler.getProject();
        String connectionName = connectionHandler.getName();
        TransactionListener transactionListener = EventManager.notify(getProject(), TransactionListener.TOPIC);
        for (TransactionAction action : actions) {
            if (action != null) {
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
                                action.getName(),
                                action.getSuccessNotificationMessage(),
                                connectionName);
                    }
                } catch (SQLException ex) {
                    NotificationUtil.sendErrorNotification(
                            project,
                            action.getName(),
                            action.getErrorNotificationMessage(),
                            connectionName,
                            ex.getMessage());
                    success = false;
                } finally {
                    // notify post-action
                    transactionListener.afterAction(connectionHandler, action, success);

                    if (action.isStatusChange()) {
                        ConnectionStatusListener statusListener = EventManager.notify(getProject(), ConnectionStatusListener.TOPIC);
                        statusListener.statusChanged(connectionHandler.getId());
                    }
                }
            }
        }
    }

    public void commit(final ConnectionHandler connectionHandler, boolean fromEditor, boolean background) {
        if (fromEditor && connectionHandler.getUncommittedChanges().size() > 1) {
            Project project = connectionHandler.getProject();
            VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
            int result = commitMultipleChangesOptionHandler.resolve(connectionHandler.getName(), selectedFile.getPresentableUrl());
            switch (result) {
                case 0: execute(connectionHandler, background, TransactionAction.COMMIT); break;
                case 1: showUncommittedChangesDialog(connectionHandler, null);
            }
        } else {
            execute(connectionHandler, background, TransactionAction.COMMIT);
        }
    }

    public void rollback(final ConnectionHandler connectionHandler, boolean fromEditor, boolean background) {
        if (fromEditor && connectionHandler.getUncommittedChanges().size() > 1) {
            Project project = connectionHandler.getProject();
            VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
            int result = rollbackMultipleChangesOptionHandler.resolve(connectionHandler.getName(), selectedFile.getPresentableUrl());
            switch (result) {
                case 0: execute(connectionHandler, background, TransactionAction.ROLLBACK); break;
                case 1: showUncommittedChangesDialog(connectionHandler, null);
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
            int result = toggleAutoCommitOptionHandler.resolve(connectionHandler.getName());
            switch (result) {
                case 0: execute(connectionHandler, true, TransactionAction.COMMIT, autoCommitAction); break;
                case 1: execute(connectionHandler, true, TransactionAction.ROLLBACK, autoCommitAction); break;
                case 2: showUncommittedChangesDialog(connectionHandler, autoCommitAction);
            }
        } else {
            execute(connectionHandler, false, autoCommitAction);
        }
    }

    public void disconnect(ConnectionHandler connectionHandler) {
        if (connectionHandler.hasUncommittedChanges()) {
            int result = disconnectOptionHandler.resolve(connectionHandler.getName());

            switch (result) {
                case 0: execute(connectionHandler, false, TransactionAction.COMMIT, TransactionAction.DISCONNECT); break;
                case 1: execute(connectionHandler, false, TransactionAction.DISCONNECT); break;
                case 2: showUncommittedChangesDialog(connectionHandler, TransactionAction.DISCONNECT);
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