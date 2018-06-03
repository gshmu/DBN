package com.dci.intellij.dbn.connection.resource.ui;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResourceMonitorDetailForm extends DBNFormImpl {
    private JTable sessionsTable;
    private JTable transactionsTable;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JBScrollPane transactionsTableScrollPane;
    private JBScrollPane sessionsTableScrollPane;
    private JButton commitButton;
    private JButton rollbackButton;

    private ConnectionHandlerRef connectionHandlerRef;
    private ResourceMonitorTransactionsTableModel transactionsTableModel;
    private ResourceMonitorSessionsTableModel sessionsTableModel;

    ResourceMonitorDetailForm(final ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
        Project project = connectionHandler.getProject();

        DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        sessionsTableModel = new ResourceMonitorSessionsTableModel(connectionHandler);
        sessionsTable = new ResourceMonitorSessionsTable(sessionsTableModel);
        sessionsTableScrollPane.setViewportView(sessionsTable);
        sessionsTableScrollPane.getViewport().setBackground(sessionsTable.getBackground());
        sessionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DatabaseSession session = sessionsTableModel.getSession(sessionsTable.getSelectedRow());
                DBNConnection connection = connectionHandler.getConnectionPool().getSessionConnection(session.getId());
                refreshForm(connectionHandler, connection);
            }
        });

        transactionsTableModel = new ResourceMonitorTransactionsTableModel(connectionHandler, null);
        transactionsTable = new ResourceMonitorTransactionsTable(transactionsTableModel);
        transactionsTableScrollPane.setViewportView(transactionsTable);
        transactionsTableScrollPane.getViewport().setBackground(transactionsTable.getBackground());

        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);

        commitButton.addActionListener(transactionActionListener);
        rollbackButton.addActionListener(transactionActionListener);

        EventUtil.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
        DisposerUtil.register(this, sessionsTable);
        DisposerUtil.register(this, transactionsTable);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
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
        public void beforeAction(ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action) {}

        @Override
        public void afterAction(ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            if (connectionHandler == getConnectionHandler() && transactionsTableModel.getConnection() == connection && succeeded) {
                refreshForm(connectionHandler, connection);
            }
        }
    };

    private ActionListener transactionActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == commitButton || source == rollbackButton) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(connectionHandler.getProject());
                DatabaseSession session = sessionsTableModel.getSession(sessionsTable.getSelectedRow());
                DBNConnection connection = connectionHandler.getConnectionPool().getSessionConnection(session.getId());
                if (source == commitButton) {
                    transactionManager.execute(
                            connectionHandler,
                            connection,
                            false,
                            TransactionAction.COMMIT,
                            null);

                } else if (source == rollbackButton) {
                    transactionManager.execute(
                            connectionHandler,
                            connection,
                            false,
                            TransactionAction.ROLLBACK,
                            null);
                }
            }
        }
    };

    private void refreshForm(final ConnectionHandler connectionHandler, final DBNConnection connection) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                if (!isDisposed()) {
                    ResourceMonitorTransactionsTableModel oldTableModel = transactionsTableModel;
                    transactionsTableModel = new ResourceMonitorTransactionsTableModel(connectionHandler, connection);
                    transactionsTable.setModel(transactionsTableModel);
                    boolean transactional = connection != null && connection.hasDataChanges();
                    commitButton.setEnabled(transactional);
                    rollbackButton.setEnabled(transactional);
                    DisposerUtil.dispose(oldTableModel);
                }
            }
        }.start();
    }
}
