package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.connection.transaction.TransactionAction.*;

public class PendingTransactionsDialog extends DBNDialog<PendingTransactionsForm> {
    private TransactionAction additionalOperation;

    public PendingTransactionsDialog(Project project, TransactionAction additionalOperation) {
        super(project, "Uncommitted changes overview", true);
        this.additionalOperation = additionalOperation;
        setModal(false);
        setResizable(true);
        init();
        EventUtil.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
    }

    @NotNull
    @Override
    protected PendingTransactionsForm createComponent() {
        return new PendingTransactionsForm(this);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                commitAllAction,
                rollbackAllAction,
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private AbstractAction commitAllAction = new AbstractAction("Commit all", Icons.CONNECTION_COMMIT) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                List<TransactionAction> actions = actions(COMMIT, additionalOperation);
                executeActions(actions);
            } finally {
                doOKAction();
            }
        }

        @Override
        public boolean isEnabled() {
            return getComponent().hasUncommittedChanges();
        }
    };

    private AbstractAction rollbackAllAction = new AbstractAction("Rollback all", Icons.CONNECTION_ROLLBACK) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                List<TransactionAction> actions = actions(ROLLBACK, additionalOperation);
                executeActions(actions);
            } finally {
                doOKAction();
            }
        }

        @Override
        public boolean isEnabled() {
            return getComponent().hasUncommittedChanges();
        }
    };

    private void executeActions(List<TransactionAction> actions) {
        DatabaseTransactionManager transactionManager = getTransactionManager();
        List<ConnectionHandler> connectionHandlers = new ArrayList<>(getComponent().getConnectionHandlers());
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            List<DBNConnection> connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
            for (DBNConnection connection : connections) {
                transactionManager.execute(connectionHandler, connection, actions, true, null);
            }

        }
    }

    private DatabaseTransactionManager getTransactionManager() {
        return DatabaseTransactionManager.getInstance(getProject());
    }

    private TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(connectionHandler.getProject());
            if (!connectionManager.hasUncommittedChanges()) {
                Dispatch.run(() -> {
                    getCancelAction().putValue(Action.NAME, "Close");
                    commitAllAction.setEnabled(false);
                    rollbackAllAction.setEnabled(false);
                });
            }
        }
    };
}
