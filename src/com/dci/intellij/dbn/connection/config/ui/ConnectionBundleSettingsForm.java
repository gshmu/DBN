package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerImpl;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettingsListener;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListCellRenderer;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.action.AddConnectionAction;
import com.dci.intellij.dbn.connection.config.action.CopyConnectionsAction;
import com.dci.intellij.dbn.connection.config.action.DuplicateConnectionAction;
import com.dci.intellij.dbn.connection.config.action.MoveConnectionDownAction;
import com.dci.intellij.dbn.connection.config.action.MoveConnectionUpAction;
import com.dci.intellij.dbn.connection.config.action.PasteConnectionAction;
import com.dci.intellij.dbn.connection.config.action.RemoveConnectionAction;
import com.dci.intellij.dbn.connection.config.action.SortConnectionsAction;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;

public class ConnectionBundleSettingsForm extends ConfigurationEditorForm<ConnectionBundleSettings> implements ListSelectionListener {
    private static final String BLANK_PANEL_ID = "BLANK_PANEL";

    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel connectionSetupPanel;
    private JList connectionsList;
    private JSplitPane mainSplitPane;

    private String currentPanelId;
    
    private Map<String, ConnectionSettingsForm> cachedForms = new HashMap<String, ConnectionSettingsForm>();

    public JList getList() {
        return connectionsList;
    }

    public ConnectionBundleSettingsForm(ConnectionBundleSettings configuration) {
        super(configuration);
        ConnectionBundle connectionBundle = configuration.getConnectionBundle();
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Connections.Setup", true,
                new AddConnectionAction(connectionsList, configuration),
                new RemoveConnectionAction(connectionsList, configuration),
                new DuplicateConnectionAction(connectionsList, configuration),
                ActionUtil.SEPARATOR,
                new CopyConnectionsAction(connectionsList, configuration),
                new PasteConnectionAction(connectionsList, configuration),
                ActionUtil.SEPARATOR,
                new MoveConnectionUpAction(connectionsList, configuration),
                new MoveConnectionDownAction(connectionsList, configuration),
                new SortConnectionsAction(connectionsList, configuration));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        connectionsList.setModel(new ConnectionListModel(connectionBundle));
        connectionsList.addListSelectionListener(this);
        connectionsList.setCellRenderer(new ConnectionConfigListCellRenderer());
        connectionsList.setFont(com.intellij.util.ui.UIUtil.getLabelFont());
        if (connectionBundle.getConnectionHandlers().size() > 0) {
            selectConnection(connectionBundle.getConnectionHandlers().get(0));
        }
        JPanel emptyPanel = new JPanel();
        connectionSetupPanel.setPreferredSize(new Dimension(500, -1));
        connectionSetupPanel.add(emptyPanel, BLANK_PANEL_ID);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, (float) 0.3);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        ConnectionBundleSettings connectionBundleSettings = getConfiguration();
        final ConnectionBundle connectionBundle = connectionBundleSettings.getConnectionBundle();

        List<ConnectionHandler> oldConnections = new ArrayList<ConnectionHandler>(connectionBundle.getConnectionHandlers().getFullList());
        List<ConnectionHandler> newConnections = new ArrayList<ConnectionHandler>();

        final AtomicBoolean listChanged = new AtomicBoolean(false);
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        if (oldConnections.size() == listModel.getSize()) {
            for (int i=0; i<oldConnections.size(); i++) {
                ConnectionSettings oldConfig = oldConnections.get(i).getSettings();
                ConnectionSettings newConfig = ((ConnectionSettings) listModel.get(i));
                if (!oldConfig.getConnectionId().equals(newConfig.getConnectionId()) ||
                        (newConfig.getSettingsEditor() != null && newConfig.getDatabaseSettings().getSettingsEditor().isConnectionActive() != oldConfig.getDatabaseSettings().isActive())) {
                    listChanged.set(true);
                    break;
                }
            }
        } else {
            listChanged.set(true);
        }

        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connectionSettings = (ConnectionSettings) listModel.getElementAt(i);
            connectionSettings.apply();

            ConnectionHandler connectionHandler = connectionBundle.getConnection(connectionSettings.getConnectionId());
            if (connectionHandler == null) {
                connectionHandler = new ConnectionHandlerImpl(connectionBundle, connectionSettings);
                connectionSettings.setNew(false);
            } else {
                oldConnections.remove(connectionHandler);
                ((ConnectionHandlerImpl)connectionHandler).setConnectionConfig(connectionSettings);
            }

            newConnections.add(connectionHandler);

        }
        connectionBundle.setConnectionHandlers(newConnections);

        // dispose old list
        DisposerUtil.dispose(oldConnections);

         new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (listChanged.get()) {
                    Project project = connectionBundle.getProject();
                    ConnectionBundleSettingsListener listener = EventManager.notify(project, ConnectionBundleSettingsListener.TOPIC);
                    listener.settingsChanged();
                }
            }
        };
    }

    public void resetFormChanges() {
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connectionSettings = (ConnectionSettings) listModel.getElementAt(i);
            connectionSettings.reset();
        }
    }

    public void selectConnection(ConnectionHandler connectionHandler) {
        connectionsList.setSelectedValue(connectionHandler.getSettings(), true);
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        try {
            Object[] selectedValues = connectionsList.getSelectedValues();
            if (selectedValues.length == 1) {
                ConnectionSettings connectionSettings = (ConnectionSettings) selectedValues[0];
                switchSettingsPanel(connectionSettings);
            } else {
                switchSettingsPanel(null);
            }
        } catch (IndexOutOfBoundsException e) {
            // fixme find out why
        }
    }

    @Override
    public void dispose() {
        for (ConnectionSettingsForm settingsForm : cachedForms.values()) {
            Disposer.dispose(settingsForm);
        }
        cachedForms.clear();
        super.dispose();
    }

    private void switchSettingsPanel(ConnectionSettings connectionSettings) {
        CardLayout cardLayout = (CardLayout) connectionSetupPanel.getLayout();
        if (connectionSettings == null) {
            cardLayout.show(connectionSetupPanel, BLANK_PANEL_ID);
        } else {

            ConnectionSettingsForm currentForm = cachedForms.get(currentPanelId);
            String selectedTabName = currentForm == null ? null : currentForm.getSelectedTabName();

            currentPanelId = connectionSettings.getConnectionId();
            if (!cachedForms.keySet().contains(currentPanelId)) {
                JComponent setupPanel = connectionSettings.createComponent();
                this.connectionSetupPanel.add(setupPanel, currentPanelId);
                cachedForms.put(currentPanelId, connectionSettings.getSettingsEditor());
            }

            ConnectionSettingsForm settingsEditor = connectionSettings.getSettingsEditor();
            if (settingsEditor != null) {
                settingsEditor.selectTab(selectedTabName);
            }

            cardLayout.show(connectionSetupPanel, currentPanelId);
        }
    }
}
