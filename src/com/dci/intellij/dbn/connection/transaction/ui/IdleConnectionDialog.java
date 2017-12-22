package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.dialog.DialogWithTimeout;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.intellij.openapi.project.Project;

public class IdleConnectionDialog extends DialogWithTimeout {
    private IdleConnectionDialogForm idleConnectionDialogForm;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBNConnection connection;

    public IdleConnectionDialog(ConnectionHandler connectionHandler, DBNConnection connection) {
        super(connectionHandler.getProject(), "Idle connection", true, TimeUtil.getSeconds(5));
        this.connectionHandlerRef = connectionHandler.getRef();
        this.connection = connection;
        idleConnectionDialogForm = new IdleConnectionDialogForm(connectionHandler, 5);
        DisposerUtil.register(this, idleConnectionDialogForm);
        setModal(false);
        init();
    }

    @Override
    protected JComponent createContentComponent() {
        return idleConnectionDialogForm.getComponent();
    }

    @Override
    public void doDefaultAction() {
        rollback();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Override
    protected void doOKAction() {
        try {
            connection.set(ResourceStatus.RESOLVING_TRANSACTION, false);
        } finally {
            super.doOKAction();
        }

    }

    @Override
    public void doCancelAction() {
        ping();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new CommitAction(),
                new RollbackAction(),
                new KeepAliveAction(),
                getHelpAction()
        };
    }

    private class CommitAction extends AbstractAction {
        CommitAction() {
            super("Commit", Icons.CONNECTION_COMMIT);
        }

        public void actionPerformed(ActionEvent e) {
            commit();
        }
    }

    private class RollbackAction extends AbstractAction {
        RollbackAction() {
            super("Rollback", Icons.CONNECTION_ROLLBACK);
        }
        public void actionPerformed(ActionEvent e) {
            rollback();
        }

    }
    private class KeepAliveAction extends AbstractAction {
        KeepAliveAction() {
            super("Keep Alive");
        }
        public void actionPerformed(ActionEvent e) {
            ping();
        }
    }

    private void commit() {
        try {
            DatabaseTransactionManager transactionManager = getTransactionManager();
            transactionManager.execute(getConnectionHandler(), connection, true, TransactionAction.COMMIT, TransactionAction.DISCONNECT_IDLE);
        } finally {
            doOKAction();
        }

    }

    private void rollback() {
        try {
            DatabaseTransactionManager transactionManager = getTransactionManager();
            transactionManager.execute(getConnectionHandler(), connection, true, TransactionAction.ROLLBACK_IDLE, TransactionAction.DISCONNECT_IDLE);
        } finally {
            doOKAction();
        }
    }

    private void ping() {
        try {
            DatabaseTransactionManager transactionManager = getTransactionManager();
            transactionManager.execute(getConnectionHandler(), connection, true, TransactionAction.KEEP_ALIVE);
        } finally {
            doOKAction();
        }
    }

    private DatabaseTransactionManager getTransactionManager() {
        Project project = getProject();
        return DatabaseTransactionManager.getInstance(project);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            connection = null;
        }
    }
}
