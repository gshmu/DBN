package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.Dispatch;
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
import java.awt.event.ActionListener;
import java.util.List;

import static com.dci.intellij.dbn.connection.transaction.TransactionAction.actions;


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

    PendingTransactionsDetailForm(ConnectionHandler connectionHandler, TransactionAction additionalOperation, boolean showActions) {
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
            ActionListener actionListener = e -> {
                Project project1 = connectionHandler.getProject();
                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project1);
                Object source = e.getSource();
                if (source == commitButton) {
                    List<TransactionAction> actions = actions(TransactionAction.COMMIT, additionalOperation);
                    transactionManager.execute(connectionHandler, null, actions, false, null);
                } else if (source == rollbackButton) {
                    List<TransactionAction> actions = actions(TransactionAction.ROLLBACK, additionalOperation);
                    transactionManager.execute(connectionHandler, null, actions, false, null);
                }
            };

            commitButton.addActionListener(actionListener);
            rollbackButton.addActionListener(actionListener);

        }
        EventUtil.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
        DisposerUtil.register(this, changesTable);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.getnn();
    }

    @NotNull
    public List<DBNConnection> getConnections() {
        return tableModel.getConnections();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            if (connectionHandler == getConnectionHandler() && succeeded) {
                refreshForm(connectionHandler);
            }
        }
    };

    private void refreshForm(ConnectionHandler connectionHandler) {
        Dispatch.invoke(() -> {
            checkDisposed();
            PendingTransactionsTableModel oldTableModel = tableModel;
            tableModel = new PendingTransactionsTableModel(connectionHandler);
            changesTable.setModel(tableModel);
            commitButton.setEnabled(false);
            rollbackButton.setEnabled(false);
            DisposerUtil.dispose(oldTableModel);
        });
    }
}
