package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
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
    private final PendingTransactionsTable pendingTransactionsTable;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JScrollPane changesTableScrollPane;
    private JButton commitButton;
    private JButton rollbackButton;
    private JPanel transactionActionsPanel;

    private final ConnectionHandlerRef connectionHandler;

    PendingTransactionsDetailForm(@NotNull DBNComponent parent, @NotNull ConnectionHandler connectionHandler, TransactionAction additionalOperation, boolean showActions) {
        super(parent);
        this.connectionHandler = connectionHandler.getRef();

        DBNHeaderForm headerForm = new DBNHeaderForm(this, connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        pendingTransactionsTable = new PendingTransactionsTable(this, new PendingTransactionsTableModel(connectionHandler));
        changesTableScrollPane.setViewportView(pendingTransactionsTable);
        changesTableScrollPane.getViewport().setBackground(pendingTransactionsTable.getBackground());

        transactionActionsPanel.setVisible(showActions);
        if (showActions) {
            ActionListener actionListener = e -> {
                Project project = connectionHandler.getProject();
                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
                List<DBNConnection> connections = pendingTransactionsTable.getSelectedConnections();
                Object source = e.getSource();

                for (DBNConnection connection : connections) {
                    if (source == commitButton) {
                        List<TransactionAction> actions = actions(TransactionAction.COMMIT, additionalOperation);
                        transactionManager.execute(connectionHandler, connection, actions, false, null);
                    } else if (source == rollbackButton) {
                        List<TransactionAction> actions = actions(TransactionAction.ROLLBACK, additionalOperation);
                        transactionManager.execute(connectionHandler, connection, actions, false, null);
                    }
                }
            };

            commitButton.addActionListener(actionListener);
            commitButton.setIcon(Icons.CONNECTION_COMMIT);

            rollbackButton.addActionListener(actionListener);
            rollbackButton.setIcon(Icons.CONNECTION_ROLLBACK);

            pendingTransactionsTable.getSelectionModel().addListSelectionListener(e -> updateTransactionActions());
            updateTransactionActions();

        }
        ProjectEvents.subscribe(ensureProject(), this, TransactionListener.TOPIC, transactionListener);
    }

    private void updateTransactionActions() {
        List<DBNConnection> connections = pendingTransactionsTable.getSelectedConnections();
        boolean selectionAvailable = connections.size() > 0;
        commitButton.setEnabled(selectionAvailable);
        rollbackButton.setEnabled(selectionAvailable);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    public List<DBNConnection> getConnections() {
        return pendingTransactionsTable.getModel().getTransactionalConnections();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private final TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            if (connectionHandler == getConnectionHandler() && succeeded) {
                refreshForm(connectionHandler);
            }
        }
    };

    private void refreshForm(ConnectionHandler connectionHandler) {
        Dispatch.run(() -> {
            checkDisposed();
            PendingTransactionsTableModel tableModel = new PendingTransactionsTableModel(connectionHandler);
            pendingTransactionsTable.setModel(tableModel);
            commitButton.setEnabled(false);
            rollbackButton.setEnabled(false);
        });
    }
}
