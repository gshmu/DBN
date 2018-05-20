package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PendingTransactionsDetailForm extends DBNFormImpl {
    private JTable changesTable;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JScrollPane changesTableScrollPane;
    private JButton commitButton;
    private JButton rollbackButton;
    private JPanel transactionActionsPanel;

    private ConnectionHandlerRef connectionHandlerRef;
    private PendingTransactionsTableModel tableModel;

    public PendingTransactionsDetailForm(final ConnectionHandler connectionHandler, final TransactionAction additionalOperation, boolean showActions) {
        this.connectionHandlerRef = connectionHandler.getRef();
        Project project = connectionHandler.getProject();

        DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        tableModel = new PendingTransactionsTableModel(connectionHandler);
        changesTable = new PendingTransactionsTable(tableModel);
        changesTableScrollPane.setViewportView(changesTable);
        changesTableScrollPane.getViewport().setBackground(changesTable.getBackground());

        transactionActionsPanel.setVisible(showActions);
        if (showActions) {
            final ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(connectionHandler.getProject());
                    Object source = e.getSource();
                    if (source == commitButton) {
                        transactionManager.execute(connectionHandler, null, false, TransactionAction.COMMIT, additionalOperation);
                    } else if (source == rollbackButton) {
                        transactionManager.execute(connectionHandler, null, false, TransactionAction.ROLLBACK, additionalOperation);
                    }
                }
            };

            commitButton.addActionListener(actionListener);
            rollbackButton.addActionListener(actionListener);

        }
        EventUtil.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
        DisposerUtil.register(this, changesTable);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @NotNull
    public List<DBNConnection> getConnections() {
        return tableModel.getConnections();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
        transactionListener = null;
    }

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void beforeAction(ConnectionHandler connectionHandler, TransactionAction action) {
        }

        @Override
        public void afterAction(ConnectionHandler connectionHandler, TransactionAction action, boolean succeeded) {
            if (connectionHandler == getConnectionHandler() && succeeded) {
                refreshForm(connectionHandler);
            }
        }
    };

    private void refreshForm(final ConnectionHandler connectionHandler) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                if (!isDisposed()) {
                    PendingTransactionsTableModel oldTableModel = tableModel;
                    tableModel = new PendingTransactionsTableModel(connectionHandler);
                    changesTable.setModel(tableModel);
                    commitButton.setEnabled(false);
                    rollbackButton.setEnabled(false);
                    DisposerUtil.dispose(oldTableModel);
                }
            }
        }.start();
    }
}
