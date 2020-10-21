package com.dci.intellij.dbn.connection.resource.ui;

import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceMonitorForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel detailsPanel;
    private JList<ConnectionHandler> connectionsList;
    private final List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    private final Map<ConnectionId, ResourceMonitorDetailForm> resourceMonitorForms = DisposableContainer.map(this);

    ResourceMonitorForm(ResourceMonitorDialog parentComponent) {
        super(parentComponent);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        connectionsList.addListSelectionListener(e -> {
            ConnectionHandler connectionHandler = connectionsList.getSelectedValue();
            showChangesForm(connectionHandler);
        });
        connectionsList.setCellRenderer(new ConnectionListCellRenderer());
        connectionsList.setSelectedIndex(0);
        updateListModel();

        ProjectEvents.subscribe(ensureProject(), this, TransactionListener.TOPIC, transactionListener);
        ProjectEvents.subscribe(ensureProject(), this, ConnectionHandlerStatusListener.TOPIC, (connectionId) -> GUIUtil.repaint(connectionsList));
    }

    private void updateListModel() {
        checkDisposed();
        DefaultListModel<ConnectionHandler> model = new DefaultListModel<>();
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
            checkDisposed();
            connectionHandlers.add(connectionHandler);
            model.addElement(connectionHandler);
        }
        connectionsList.setModel(model);
        if (model.size() > 0) {
            connectionsList.setSelectedIndex(0);
        }
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public List<ConnectionHandler> getConnectionHandlers (){
        return connectionHandlers;
    }

    private void showChangesForm(ConnectionHandler connectionHandler) {
        detailsPanel.removeAll();
        if (connectionHandler != null) {
            ConnectionId connectionId = connectionHandler.getConnectionId();
            ResourceMonitorDetailForm detailForm = resourceMonitorForms.get(connectionId);
            if (detailForm == null) {
                detailForm = new ResourceMonitorDetailForm(this, connectionHandler);
                resourceMonitorForms.put(connectionId, detailForm);
            }
            detailsPanel.add(detailForm.getComponent(), BorderLayout.CENTER);
        }

        GUIUtil.repaint(detailsPanel);
    }

    private static class ConnectionListCellRenderer extends ColoredListCellRenderer<ConnectionHandler> {

        @Override
        protected void customizeCellRenderer(@NotNull JList list, ConnectionHandler value, int index, boolean selected, boolean hasFocus) {
            setIcon(value.getIcon());
            append(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            List<DBNConnection> connections = value.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
            int changes = 0;
            for (DBNConnection connection : connections) {
                PendingTransactionBundle dataChanges = connection.getDataChanges();
                changes += dataChanges == null ? 0 : dataChanges.size();
            }

            if (changes > 0) {
                append(" (" + changes + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
    }

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private final TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            refreshForm();
        }
    };

    private void refreshForm() {
        Dispatch.run(() -> updateListModel());

    }
}
