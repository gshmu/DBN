package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.DatabaseUrlType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListCellRenderer;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.ListUtil;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConnectionBundleSettingsForm extends ConfigurationEditorForm<ConnectionBundleSettings> implements ListSelectionListener {
    private static final String BLANK_PANEL_ID = "BLANK_PANEL";

    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel connectionSetupPanel;
    private JBScrollPane connectionListScrollPane;
    private final JList<ConnectionSettings> connectionsList;

    private String currentPanelId;

    
    private final Map<String, ConnectionSettingsForm> cachedForms = DisposableContainer.map(this);

    public JList getList() {
        return connectionsList;
    }

    public ConnectionBundleSettingsForm(ConnectionBundleSettings configuration) {
        super(configuration);
        connectionsList = new JBList<>(new ConnectionListModel(configuration));
        connectionsList.addListSelectionListener(this);
        connectionsList.setCellRenderer(new ConnectionConfigListCellRenderer());
        connectionsList.setFont(com.intellij.util.ui.UIUtil.getLabelFont());
        connectionsList.setBackground(UIUtil.getTextFieldBackground());

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(actionsPanel,"", true, "DBNavigator.ActionGroup.ConnectionSettings");
        JComponent component = actionToolbar.getComponent();
        actionsPanel.add(component, BorderLayout.CENTER);
        connectionListScrollPane.setViewportView(connectionsList);

        List<ConnectionSettings> connections = configuration.getConnections();
        if (connections.size() > 0) {
            selectConnection(ConnectionId.get(connections.get(0).getId()));
        }
        JPanel emptyPanel = new JPanel();
        connectionSetupPanel.setPreferredSize(new Dimension(500, -1));
        connectionSetupPanel.add(emptyPanel, BLANK_PANEL_ID);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, (float) 0.3);

        DataManager.registerDataProvider(mainPanel, this);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        ConnectionBundleSettings connectionBundleSettings = getConfiguration();

        List<ConnectionSettings> newConnections = new ArrayList<>();
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connection = listModel.getElementAt(i);
            connection.apply();
            connection.setNew(false);
            newConnections.add(connection);
        }

        List<ConnectionSettings> connections = connectionBundleSettings.getConnections();
        connections.clear();
        connections.addAll(newConnections);
    }

    @Override
    public void resetFormChanges() {
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connectionSettings = listModel.getElementAt(i);
            connectionSettings.reset();
        }
    }

    public void selectConnection(@Nullable ConnectionId connectionId) {
        if (connectionId != null) {
            ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
            for (int i=0; i<model.size(); i++) {
                ConnectionSettings connectionSettings = model.getElementAt(i);
                if (connectionSettings.getConnectionId() == connectionId) {
                    connectionsList.setSelectedValue(connectionSettings, true);
                    break;
                }
            }

        }
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        try {
            List<ConnectionSettings> selectedValues = connectionsList.getSelectedValuesList();
            if (selectedValues.size() == 1) {
                ConnectionSettings connectionSettings = selectedValues.get(0);
                switchSettingsPanel(connectionSettings);
            } else {
                switchSettingsPanel(null);
            }
        } catch (IndexOutOfBoundsException e) {
            // fixme find out why
        }
    }

    private void switchSettingsPanel(ConnectionSettings connectionSettings) {
        CardLayout cardLayout = (CardLayout) connectionSetupPanel.getLayout();
        if (connectionSettings == null) {
            cardLayout.show(connectionSetupPanel, BLANK_PANEL_ID);
        } else {

            ConnectionSettingsForm currentForm = cachedForms.get(currentPanelId);
            String selectedTabName = currentForm == null ? null : currentForm.getSelectedTabName();

            currentPanelId = connectionSettings.getConnectionId().id();
            if (!cachedForms.containsKey(currentPanelId)) {
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


    public ConnectionId createNewConnection(@NotNull DatabaseType databaseType, @NotNull ConnectionConfigType configType) {
        ConnectionBundleSettings connectionBundleSettings = getConfiguration();
        ConnectionSettings connectionSettings = new ConnectionSettings(connectionBundleSettings, databaseType, configType);
        connectionSettings.setNew(true);
        connectionSettings.generateNewId();

        connectionBundleSettings.setModified(true);
        connectionBundleSettings.getConnections().add(connectionSettings);

        String name = "Connection";
        ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
        while (model.getConnectionConfig(name) != null) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        ConnectionDatabaseSettings connectionConfig = connectionSettings.getDatabaseSettings();
        connectionConfig.setName(name);
        int index = connectionsList.getModel().getSize();
        model.add(index, connectionSettings);
        connectionsList.setSelectedIndex(index);
        return connectionSettings.getConnectionId();
    }

    public void duplicateSelectedConnection() {
        ConnectionSettings connectionSettings = connectionsList.getSelectedValue();
        if (connectionSettings != null) {
            getConfiguration().setModified(true);
            ConnectionSettingsForm settingsEditor = connectionSettings.getSettingsEditor();
            if (settingsEditor != null) {
                try {
                    ConnectionSettings duplicate = settingsEditor.getTemporaryConfig();
                    duplicate.setNew(true);
                    String name = duplicate.getDatabaseSettings().getName();
                    ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
                    while (model.getConnectionConfig(name) != null) {
                        name = NamingUtil.getNextNumberedName(name, true);
                    }
                    duplicate.getDatabaseSettings().setName(name);
                    int selectedIndex = connectionsList.getSelectedIndex() + 1;
                    model.add(selectedIndex, duplicate);
                    connectionsList.setSelectedIndex(selectedIndex);
                } catch (ConfigurationException e) {
                    MessageUtil.showErrorDialog(getProject(), e.getMessage());
                }
            }

        }
    }

    public void removeSelectedConnections() {
        getConfiguration().setModified(true);
        List<ConnectionSettings> connectionSettings = ListUtil.removeSelectedItems(connectionsList);
        for (ConnectionSettings connectionSetting : connectionSettings) {
            connectionSetting.disposeUIResources();
        }

    }

    public void moveSelectedConnectionsUp() {
        getConfiguration().setModified(true);
        ListUtil.moveSelectedItemsUp(connectionsList);
    }

    public void moveSelectedConnectionsDown() {
        getConfiguration().setModified(true);
        ListUtil.moveSelectedItemsDown(connectionsList);
    }

    public void copyConnectionsToClipboard() {
        List<ConnectionSettings> configurations = connectionsList.getSelectedValuesList();
        try {
            Element rootElement = new Element("connection-configurations");
            for (ConnectionSettings configuration : configurations) {
                Element configElement = new Element("config");
                configuration.writeConfiguration(configElement);
                rootElement.addContent(configElement);
            }

            Document document = new Document(rootElement);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            String xmlString = outputter.outputString(document);

            CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
            copyPasteManager.setContents(new StringSelection(xmlString));
            MessageUtil.showInfoDialog(getProject(), "Config Export", "Configuration for selected connections exported to clipboard.");
        } catch (Exception ex) {
            log.error("Could not copy database configuration to clipboard", ex);
        }
    }

    public void pasteConnectionsFromClipboard() {
        try {
            String clipboardData = ClipboardUtil.getStringContent();
            if (clipboardData != null) {
                Document xmlDocument = CommonUtil.createXMLDocument(new ByteArrayInputStream(clipboardData.getBytes()));
                if (xmlDocument != null) {
                    Element rootElement = xmlDocument.getRootElement();
                    List<Element> configElements = rootElement.getChildren();
                    ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
                    int index = connectionsList.getModel().getSize();
                    List<Integer> selectedIndexes = new ArrayList<Integer>();
                    ConnectionBundleSettings configuration = getConfiguration();
                    for (Element configElement : configElements) {
                        ConnectionSettings clone = new ConnectionSettings(configuration);
                        clone.readConfiguration(configElement);
                        clone.setNew(true);
                        clone.generateNewId();

                        ConnectionDatabaseSettings databaseSettings = clone.getDatabaseSettings();
                        String name = databaseSettings.getName();
                        while (model.getConnectionConfig(name) != null) {
                            name = NamingUtil.getNextNumberedName(name, true);
                        }
                        databaseSettings.setName(name);
                        model.add(index, clone);
                        selectedIndexes.add(index);
                        configuration.setModified(true);
                        index++;
                    }

                    connectionsList.setSelectedIndices(ArrayUtils.toPrimitive(selectedIndexes.toArray(new Integer[0])));

                }
            }
        } catch (Exception ex) {
            log.error("Could not paste database configuration from clipboard", ex);
        }
    }

    public void importTnsNames(List<TnsName> tnsNames) {
        ConnectionBundleSettings connectionBundleSettings = getConfiguration();
        ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
        int index = connectionsList.getModel().getSize();
        List<Integer> selectedIndexes = new ArrayList<>();

        for (TnsName tnsName : tnsNames) {
            ConnectionSettings connectionSettings = new ConnectionSettings(connectionBundleSettings, DatabaseType.ORACLE, ConnectionConfigType.BASIC);
            connectionSettings.setNew(true);
            connectionSettings.generateNewId();
            connectionBundleSettings.setModified(true);
            connectionBundleSettings.getConnections().add(connectionSettings);

            ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
            String name = tnsName.getName();
            while (model.getConnectionConfig(name) != null) {
                name = NamingUtil.getNextNumberedName(name, true);
            }

            DatabaseInfo databaseInfo = databaseSettings.getDatabaseInfo();
            databaseInfo.setHost(tnsName.getHost());
            databaseInfo.setPort(tnsName.getPort());


            String sid = tnsName.getSid();
            String service = tnsName.getServiceName();
            if (StringUtil.isNotEmpty(sid)) {
                databaseInfo.setDatabase(sid);
                databaseInfo.setUrlType(DatabaseUrlType.SID);
            } else if (StringUtil.isNotEmpty(service)) {
                databaseInfo.setDatabase(service);
                databaseInfo.setUrlType(DatabaseUrlType.SERVICE);
            }
            databaseSettings.setName(name);
            databaseSettings.setDatabaseType(DatabaseType.ORACLE);
            databaseSettings.setDriverSource(DriverSource.BUILTIN);

            model.add(index, connectionSettings);
            selectedIndexes.add(index);
            connectionBundleSettings.setModified(true);
            index++;
        }
        connectionsList.setSelectedIndices(ArrayUtils.toPrimitive(selectedIndexes.toArray(new Integer[0])));
    }


    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (DataKeys.CONNECTION_BUNDLE_SETTINGS.is(dataId)) {
            return ConnectionBundleSettingsForm.this;
        }
        return super.getData(dataId);
    }

    public int getSelectionSize() {
        return connectionsList.getSelectedValuesList().size();
    }
}
