package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.dispose.DisposableContainers;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingTransactionsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel detailsPanel;
    private JList<ConnectionHandler> connectionsList;
    private final List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    private final Map<ConnectionId, PendingTransactionsDetailForm> uncommittedChangeForms = DisposableContainers.map(this);

    PendingTransactionsForm(PendingTransactionsDialog parentComponent) {
        super(parentComponent);
        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        connectionsList.addListSelectionListener(e -> {
            ConnectionHandler connection = connectionsList.getSelectedValue();
            showChangesForm(connection);
        });

        connectionsList.setCellRenderer(new ListCellRenderer());
        connectionsList.setSelectedIndex(0);
        updateListModel();

        ProjectEvents.subscribe(ensureProject(), this, TransactionListener.TOPIC, transactionListener);
    }

    private void updateListModel() {
        checkDisposed();
        DefaultListModel<ConnectionHandler> model = new DefaultListModel<>();
        ConnectionManager connectionManager = ConnectionManager.getInstance(ensureProject());
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        for (ConnectionHandler connection : connectionBundle.getConnections()) {
            if (connection.hasUncommittedChanges()) {
                connectionHandlers.add(connection);
                model.addElement(connection);
            }
        }
        connectionsList.setModel(model);
        if (model.size() > 0) {
            connectionsList.setSelectedIndex(0);
        }
    }

    boolean hasUncommittedChanges() {
        for (ConnectionHandler connection : connectionHandlers) {
            if (connection.hasUncommittedChanges()) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public List<ConnectionHandler> getConnectionHandlers (){
        return connectionHandlers;
    }

    public void showChangesForm(ConnectionHandler connection) {
        detailsPanel.removeAll();
        if (connection != null) {
            ConnectionId connectionId = connection.getConnectionId();
            PendingTransactionsDetailForm pendingTransactionsForm = uncommittedChangeForms.get(connectionId);
            if (pendingTransactionsForm == null) {
                pendingTransactionsForm = new PendingTransactionsDetailForm(this, connection, null, true);
                uncommittedChangeForms.put(connectionId, pendingTransactionsForm);
            }
            detailsPanel.add(pendingTransactionsForm.getComponent(), BorderLayout.CENTER);
        }

        GUIUtil.repaint(detailsPanel);
    }

    private static class ListCellRenderer extends ColoredListCellRenderer<Object> {
        @Override
        protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
            ConnectionHandler connection = (ConnectionHandler) value;
            setIcon(connection.getIcon());
            append(connection.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            List<DBNConnection> connections = connection.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
            int changes = 0;
            for (DBNConnection conn : connections) {
                PendingTransactionBundle dataChanges = conn.getDataChanges();
                changes += dataChanges == null ? 0 : dataChanges.size();
            }

            append(" (" + changes + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
    }

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private final TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connection, DBNConnection conn, TransactionAction action, boolean succeeded) {
            refreshForm();
        }
    };

    private void refreshForm() {
        Dispatch.run(() -> updateListModel());
    }


    @Override
    public void disposeInner() {
        connectionHandlers.clear();
        super.disposeInner();
    }

}
