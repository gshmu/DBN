package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;

public class UncommittedChangesDialog extends DBNDialog {
    private UncommittedChangesForm mainComponent;
    private ConnectionHandler connectionHandler;
    private TransactionAction additionalOperation;

    public UncommittedChangesDialog(ConnectionHandler connectionHandler, TransactionAction additionalOperation, boolean showActions) {
        super(connectionHandler.getProject(), "Uncommitted Changes", true);
        this.connectionHandler = connectionHandler;
        this.additionalOperation = additionalOperation;
        mainComponent = new UncommittedChangesForm(connectionHandler, additionalOperation, showActions);
        setModal(false);
        setResizable(true);
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.UncommittedChanges";
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new CommitAction(),
                new RollbackAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private class CommitAction extends AbstractAction {
        public CommitAction() {
            super("Commit", Icons.CONNECTION_COMMIT);
        }

        public void actionPerformed(ActionEvent e) {
            ConnectionHandler commitConnectionHandler = connectionHandler;
            DatabaseTransactionManager transactionManager = getTransactionManager();
            doOKAction();
            transactionManager.execute(commitConnectionHandler, true, TransactionAction.COMMIT, additionalOperation);
        }
    }

    private class RollbackAction extends AbstractAction {
        public RollbackAction() {
            super("Rollback", Icons.CONNECTION_ROLLBACK);
        }

        public void actionPerformed(ActionEvent e) {
            ConnectionHandler commitConnectionHandler = connectionHandler;
            DatabaseTransactionManager transactionManager = getTransactionManager();
            doOKAction();
            transactionManager.execute(commitConnectionHandler, true, TransactionAction.ROLLBACK, additionalOperation);
        }
    }

    private DatabaseTransactionManager getTransactionManager() {
        Project project = connectionHandler.getProject();
        return DatabaseTransactionManager.getInstance(project);
    }


    @Nullable
    protected JComponent createCenterPanel() {
        return mainComponent.getComponent();
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            mainComponent.dispose();
            mainComponent = null;
            connectionHandler = null;
        }
    }
}
