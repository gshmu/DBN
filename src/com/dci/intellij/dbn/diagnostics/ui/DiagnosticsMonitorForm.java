package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DiagnosticsMonitorForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel detailsPanel;
    private JList<ConnectionHandler> connectionsList;
    private int tabSelectionIndex;

    private final Map<ConnectionId, DiagnosticsMonitorDetailsForm> resourceMonitorForms = DisposableContainer.map(this);

    public DiagnosticsMonitorForm(@Nullable DiagnosticsMonitorDialog parent) {
        super(parent);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        connectionsList.addListSelectionListener(e -> {
            ConnectionHandler connectionHandler = connectionsList.getSelectedValue();
            showDetailsForm(connectionHandler);
        });
        connectionsList.setCellRenderer(new ConnectionListCellRenderer());

        DefaultListModel<ConnectionHandler> model = new DefaultListModel<>();
        ConnectionManager connectionManager = ConnectionManager.getInstance(ensureProject());
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        List<ConnectionHandler> connectionHandlers = connectionBundle.getConnectionHandlers();
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            model.addElement(connectionHandler);
        }
        connectionsList.setModel(model);
        connectionsList.setSelectedIndex(0);
    }

    private void showDetailsForm(ConnectionHandler connectionHandler) {
        detailsPanel.removeAll();
        if (connectionHandler != null) {
            ConnectionId connectionId = connectionHandler.getConnectionId();
            DiagnosticsMonitorDetailsForm detailForm = resourceMonitorForms.get(connectionId);
            if (detailForm == null) {
                detailForm = new DiagnosticsMonitorDetailsForm(this, connectionHandler);
                resourceMonitorForms.put(connectionId, detailForm);
            }
            detailsPanel.add(detailForm.getComponent(), BorderLayout.CENTER);
            detailForm.selectTab(tabSelectionIndex);
        }

        GUIUtil.repaint(detailsPanel);
    }

    public void setTabSelectionIndex(int tabSelectionIndex) {
        this.tabSelectionIndex = tabSelectionIndex;
    }

    private static class ConnectionListCellRenderer extends ColoredListCellRenderer<ConnectionHandler> {

        @Override
        protected void customizeCellRenderer(@NotNull JList list, ConnectionHandler value, int index, boolean selected, boolean hasFocus) {
            setIcon(value.getIcon());
            append(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
