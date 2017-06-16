package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListCellRenderer;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.ListUtil;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NonNls;
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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionBundleSettingsForm extends ConfigurationEditorForm<ConnectionBundleSettings> implements ListSelectionListener, DataProviderSupplier {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private static final String BLANK_PANEL_ID = "BLANK_PANEL";

    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel connectionSetupPanel;
    private JBScrollPane connectionListScrollPane;
    private JList connectionsList;

    private String currentPanelId;

    
    private Map<String, ConnectionSettingsForm> cachedForms = new HashMap<String, ConnectionSettingsForm>();

    public JList getList() {
        return connectionsList;
    }

    public ConnectionBundleSettingsForm(ConnectionBundleSettings configuration) {
        super(configuration);
        connectionsList = new JBList(new ConnectionListModel(configuration));
        connectionsList.addListSelectionListener(this);
        connectionsList.setCellRenderer(new ConnectionConfigListCellRenderer());
        connectionsList.setFont(com.intellij.util.ui.UIUtil.getLabelFont());
        connectionsList.setBackground(UIUtil.getTextFieldBackground());

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.ConnectionSettings");
        actionToolbar.setTargetComponent(actionsPanel);
        JComponent component = actionToolbar.getComponent();
        actionsPanel.add(component, BorderLayout.CENTER);
        connectionListScrollPane.setViewportView(connectionsList);

        List<ConnectionSettings> connections = configuration.getConnections();
        if (connections.size() > 0) {
            selectConnection(connections.get(0).getId());
        }
        JPanel emptyPanel = new JPanel();
        connectionSetupPanel.setPreferredSize(new Dimension(500, -1));
        connectionSetupPanel.add(emptyPanel, BLANK_PANEL_ID);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, (float) 0.3);

        ActionUtil.registerDataProvider(mainPanel, this);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        ConnectionBundleSettings connectionBundleSettings = getConfiguration();

        List<ConnectionSettings> newConnections = new ArrayList<ConnectionSettings>();
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connection = (ConnectionSettings) listModel.getElementAt(i);
            connection.apply();
            connection.setNew(false);
            newConnections.add(connection);
        }

        List<ConnectionSettings> connections = connectionBundleSettings.getConnections();
        connections.clear();
        connections.addAll(newConnections);
    }

    public void resetFormChanges() {
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connectionSettings = (ConnectionSettings) listModel.getElementAt(i);
            connectionSettings.reset();
        }
    }

    public void selectConnection(@Nullable String connectionId) {
        if (connectionId != null) {
            ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
            for (int i=0; i<model.size(); i++) {
                ConnectionSettings connectionSettings = (ConnectionSettings) model.getElementAt(i);
                if (connectionSettings.getConnectionId().equals(connectionId)) {
                    connectionsList.setSelectedValue(connectionSettings, true);
                    break;
                }
            }

        }
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


    public String createNewConnection(@NotNull DatabaseType databaseType, @NotNull ConnectionConfigType configType) {
        ConnectionBundleSettings connectionBundleSettings = getConfiguration();
        connectionBundleSettings.setModified(true);
        ConnectionSettings connectionSettings = new ConnectionSettings(connectionBundleSettings, databaseType, configType);
        connectionSettings.setNew(true);
        connectionSettings.generateNewId();

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
        ConnectionSettings connectionSettings = (ConnectionSettings) connectionsList.getSelectedValue();
        if (connectionSettings != null) {
            getConfiguration().setModified(true);
            ConnectionSettingsForm settingsEditor = connectionSettings.getSettingsEditor();
            if (settingsEditor != null) {
                ConnectionSettings duplicate = null;
                try {
                    duplicate = settingsEditor.getTemporaryConfig();
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
        ListUtil.removeSelectedItems(connectionsList);
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
        Object[] configurations = connectionsList.getSelectedValues();
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Element rootElement = new Element("connection-configurations");
            for (Object o : configurations) {
                ConnectionSettings configuration = (ConnectionSettings) o;
                Element configElement = new Element("config");
                configuration.writeConfiguration(configElement);
                rootElement.addContent(configElement);
            }

            Document document = new Document(rootElement);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            String xmlString = outputter.outputString(document);
            clipboard.setContents(ClipboardUtil.createXmlContent(xmlString), null);
        } catch (Exception ex) {
            LOGGER.error("Could not copy database configuration to clipboard", ex);
        }
    }

    public void pasteConnectionsFromClipboard() {
        try {
            String clipboardData = ClipboardUtil.getStringContent();
            if (clipboardData != null) {
                Document xmlDocument = CommonUtil.createXMLDocument(new ReaderInputStream(new StringReader(clipboardData), "UTF-8"));
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

                    connectionsList.setSelectedIndices(ArrayUtils.toPrimitive(selectedIndexes.toArray(new Integer[selectedIndexes.size()])));

                }
            }
        } catch (Exception ex) {
            LOGGER.error("Could not paste database configuration from clipboard", ex);
        }
    }

    public void importTnsNames(List<TnsName> tnsNames) {
        ConnectionBundleSettings configuration = getConfiguration();
        ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
        int index = connectionsList.getModel().getSize();
        List<Integer> selectedIndexes = new ArrayList<Integer>();

        for (TnsName tnsName : tnsNames) {
            ConnectionSettings clone = new ConnectionSettings(configuration);
            clone.setNew(true);
            clone.generateNewId();
            ConnectionDatabaseSettings databaseSettings = clone.getDatabaseSettings();
            String name = tnsName.getName();
            while (model.getConnectionConfig(name) != null) {
                name = NamingUtil.getNextNumberedName(name, true);
            }

            DatabaseInfo databaseInfo = databaseSettings.getDatabaseInfo();
            databaseInfo.setHost(tnsName.getHost());
            databaseInfo.setPort(tnsName.getPort());
            databaseInfo.setDatabase(tnsName.getSid());
            databaseSettings.setName(name);
            databaseSettings.setDatabaseType(DatabaseType.ORACLE);
            databaseSettings.setDriverSource(DriverSource.BUILTIN);

            model.add(index, clone);
            selectedIndexes.add(index);
            configuration.setModified(true);
            index++;
        }
        connectionsList.setSelectedIndices(ArrayUtils.toPrimitive(selectedIndexes.toArray(new Integer[selectedIndexes.size()])));
    }



    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.CONNECTION_BUNDLE_SETTINGS.is(dataId)) {
                return ConnectionBundleSettingsForm.this;
            }
            return null;
        }
    };

    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public int getSelectionSize() {
        return connectionsList.getSelectedValues().length;
    }
}
