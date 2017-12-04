package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.intellij.openapi.project.Project;

public class UncommittedChangesDialog extends DBNDialog<UncommittedChangesForm> {
    private ConnectionHandlerRef connectionHandlerRef;
    private TransactionAction additionalOperation;

    public UncommittedChangesDialog(ConnectionHandler connectionHandler, TransactionAction additionalOperation, boolean showActions) {
        super(connectionHandler.getProject(), "Uncommitted changes", true);
        this.connectionHandlerRef = connectionHandler.getRef();
        this.additionalOperation = additionalOperation;
        component = new UncommittedChangesForm(connectionHandler, additionalOperation, showActions);
        setModal(false);
        setResizable(true);
        init();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
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
        CommitAction() {
            super("Commit", Icons.CONNECTION_COMMIT);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                DatabaseTransactionManager transactionManager = getTransactionManager();
                ConnectionHandler connectionHandler = getConnectionHandler();
                List<DBNConnection> connections = component.getConnections();
                for (DBNConnection connection : connections) {
                    transactionManager.execute(connectionHandler, connection, true, TransactionAction.COMMIT, additionalOperation);
                }

            } finally {
                doOKAction();
            }
        }
    }

    private class RollbackAction extends AbstractAction {
        RollbackAction() {
            super("Rollback", Icons.CONNECTION_ROLLBACK);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                DatabaseTransactionManager transactionManager = getTransactionManager();
                ConnectionHandler connectionHandler = getConnectionHandler();
                List<DBNConnection> connections = component.getConnections();
                for (DBNConnection connection : connections) {
                    transactionManager.execute(connectionHandler, connection, true, TransactionAction.ROLLBACK, additionalOperation);
                }

            } finally {
                doOKAction();
            }
        }
    }

    private DatabaseTransactionManager getTransactionManager() {
        Project project = getConnectionHandler().getProject();
        return DatabaseTransactionManager.getInstance(project);
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
