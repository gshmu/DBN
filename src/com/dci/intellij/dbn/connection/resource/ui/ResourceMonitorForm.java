package com.dci.intellij.dbn.connection.resource.ui;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.SimpleTextAttributes;

public class ResourceMonitorForm extends DBNFormImpl<ResourceMonitorDialog> {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel detailsPanel;
    private JList<ConnectionHandler> connectionsList;
    private List<ConnectionHandler> connectionHandlers = new ArrayList<ConnectionHandler>();

    private Map<ConnectionId, ResourceMonitorDetailForm> resourceMonitorForms = new HashMap<ConnectionId, ResourceMonitorDetailForm>();

    ResourceMonitorForm(ResourceMonitorDialog parentComponent) {
        super(parentComponent);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        connectionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ConnectionHandler connectionHandler = connectionsList.getSelectedValue();
                showChangesForm(connectionHandler);
            }
        });
        connectionsList.setCellRenderer(new ConnectionListCellRenderer());
        connectionsList.setSelectedIndex(0);
        updateListModel();

        Project project = getProject();
        EventUtil.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
    }

    private void updateListModel() {
        DefaultListModel<ConnectionHandler> model = new DefaultListModel<ConnectionHandler>();
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
            connectionHandlers.add(connectionHandler);
            model.addElement(connectionHandler);
        }
        connectionsList.setModel(model);
        if (model.size() > 0) {
            connectionsList.setSelectedIndex(0);
        }
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        transactionListener = null;
        connectionHandlers = null;
    }

    public List<ConnectionHandler> getConnectionHandlers (){
        return connectionHandlers;
    }

    public void showChangesForm(ConnectionHandler connectionHandler) {
        detailsPanel.removeAll();
        if (connectionHandler != null) {
            ConnectionId connectionId = connectionHandler.getId();
            ResourceMonitorDetailForm detailForm = resourceMonitorForms.get(connectionId);
            if (detailForm == null) {
                detailForm = new ResourceMonitorDetailForm(connectionHandler, null, true);
                resourceMonitorForms.put(connectionId, detailForm);
            }
            detailsPanel.add(detailForm.getComponent(), BorderLayout.CENTER);
        }

        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private class ConnectionListCellRenderer extends ColoredListCellRenderer<ConnectionHandler> {

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

            append(" (" + changes + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
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
            refreshForm();
        }
    };

    private void refreshForm() {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                if (!isDisposed()) {
                    updateListModel();
                }
            }
        }.start();

    }
}
