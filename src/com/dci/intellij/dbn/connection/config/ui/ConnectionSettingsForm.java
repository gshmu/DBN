package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentConfigLocalListener;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.ui.tab.TabbedPaneUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSshTunnelSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.ui.UIUtil;

public class ConnectionSettingsForm extends CompositeConfigurationEditorForm<ConnectionSettings>{
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private JButton infoButton;
    private JButton testButton;
    private TabbedPane configTabbedPane;

    private DBNHeaderForm headerForm;

    public ConnectionSettingsForm(ConnectionSettings connectionSettings) {
        super(connectionSettings);
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        configTabbedPane = new TabbedPane(this);
        contentPanel.add(configTabbedPane, BorderLayout.CENTER);


        TabInfo connectionTabInfo = new TabInfo(new JBScrollPane(databaseSettings.createComponent()));
        connectionTabInfo.setText("Connection");
        configTabbedPane.addTab(connectionTabInfo);

        ConnectionPropertiesSettings propertiesSettings = connectionSettings.getPropertiesSettings();
        TabInfo propertiesTabInfo = new TabInfo(new JBScrollPane(propertiesSettings.createComponent()));
        propertiesTabInfo.setText("Properties");
        configTabbedPane.addTab(propertiesTabInfo);

        ConnectionSshTunnelSettings sshTunnelSettings = connectionSettings.getSshTunnelSettings();
        TabInfo sshTunnelTabInfo = new TabInfo(new JBScrollPane(sshTunnelSettings.createComponent()));
        sshTunnelTabInfo.setText("SSH");
        configTabbedPane.addTab(sshTunnelTabInfo);

        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        TabInfo detailsTabInfo = new TabInfo(new JBScrollPane(detailSettings.createComponent()));
        detailsTabInfo.setText("Details");
        configTabbedPane.addTab(detailsTabInfo);

        ConnectionFilterSettings filterSettings = connectionSettings.getFilterSettings();
        TabInfo filtersTabInfo = new TabInfo(new JBScrollPane(filterSettings.createComponent()));
        filtersTabInfo.setText("Filters");
        configTabbedPane.addTab(filtersTabInfo);

        ConnectionDatabaseSettingsForm databaseSettingsForm = (ConnectionDatabaseSettingsForm) databaseSettings.getSettingsEditor();
        ConnectionDetailSettingsForm detailSettingsForm = detailSettings.getSettingsEditor();

        ConnectivityStatus connectivityStatus = databaseSettings.getConnectivityStatus();
        Icon icon = connectionSettings.isNew() ? Icons.CONNECTION_NEW :
                   !databaseSettings.isActive() ? Icons.CONNECTION_DISABLED :
                   connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE :
                   connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;

        headerForm = new DBNHeaderForm(connectionSettings.getDatabaseSettings().getName(), icon, detailSettings.getEnvironmentType().getColor());
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        Project project = databaseSettings.getProject();
        EventUtil.subscribe(project, this, ConnectionPresentationChangeListener.TOPIC, connectionPresentationChangeListener);
        EventUtil.subscribe(project, this, EnvironmentConfigLocalListener.TOPIC, environmentConfigListener);

        databaseSettingsForm.notifyPresentationChanges();
        detailSettingsForm.notifyPresentationChanges();

        registerComponent(testButton);
        registerComponent(infoButton);
    }

    public ConnectionSettings getTemporaryConfig() throws ConfigurationException {
        ConnectionSettings configuration = getConfiguration();
        ConnectionSettings clone = configuration.clone();
        ConnectionDatabaseSettingsForm databaseSettingsEditor = (ConnectionDatabaseSettingsForm) configuration.getDatabaseSettings().getSettingsEditor();
        if(databaseSettingsEditor != null) {
            databaseSettingsEditor.applyFormChanges(clone.getDatabaseSettings());
        }
        ConnectionPropertiesSettingsForm propertiesSettingsEditor = configuration.getPropertiesSettings().getSettingsEditor();
        if (propertiesSettingsEditor != null) {
            propertiesSettingsEditor.applyFormChanges(clone.getPropertiesSettings());
        }

        ConnectionSshTunnelSettingsForm sshTunnelSettingsForm = configuration.getSshTunnelSettings().getSettingsEditor();
        if (sshTunnelSettingsForm != null) {
            sshTunnelSettingsForm.applyFormChanges(clone.getSshTunnelSettings());
        }

        return clone;
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == testButton || source == infoButton) {

                    ConnectionSettings configuration = getConfiguration();
                    ConnectionSettingsForm connectionSettingsForm = configuration.getSettingsEditor();
                    if (connectionSettingsForm != null) {
                        try {
                            ConnectionSettings temporaryConfig = connectionSettingsForm.getTemporaryConfig();
                            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());

                            if (source == testButton) connectionManager.testConfigConnection(temporaryConfig, true);
                            if (source == infoButton) {
                                ConnectionDetailSettingsForm detailSettingsForm = configuration.getDetailSettings().getSettingsEditor();
                                if (detailSettingsForm != null) {
                                    EnvironmentType environmentType = detailSettingsForm.getSelectedEnvironmentType();
                                    connectionManager.showConnectionInfo(temporaryConfig, environmentType);
                                }
                            }

                            ConnectionBundleSettingsForm bundleSettingsForm = configuration.getParent().getSettingsEditor();
                            if (bundleSettingsForm != null) {
                                JList connectionList = bundleSettingsForm.getList();
                                connectionList.revalidate();
                                connectionList.repaint();
                                ConnectionDatabaseSettingsForm settingsEditor = (ConnectionDatabaseSettingsForm) configuration.getDatabaseSettings().getSettingsEditor();
                                if (settingsEditor != null) {
                                    settingsEditor.notifyPresentationChanges();
                                }
                            }
                        } catch (ConfigurationException e1) {
                            MessageUtil.showErrorDialog(getProject(), "Configuration error", e1.getMessage());
                        }
                    }
                }
            }
        };
    }



    public void selectTab(String tabName) {
        TabbedPaneUtil.selectTab(configTabbedPane, tabName);        
    }
    
    public String getSelectedTabName() {
        return TabbedPaneUtil.getSelectedTabName(configTabbedPane);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    ConnectionPresentationChangeListener connectionPresentationChangeListener = new ConnectionPresentationChangeListener() {
        @Override
        public void presentationChanged(final String name, final Icon icon, final Color color, final String connectionId, DatabaseType databaseType) {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    ConnectionSettings configuration = getConfiguration();
                    if (configuration != null && configuration.getConnectionId().equals(connectionId)) {
                        if (name != null) headerForm.setTitle(name);
                        if (icon != null) headerForm.setIcon(icon);
                        if (color != null) headerForm.setBackground(color); else headerForm.setBackground(UIUtil.getPanelBackground());
                        //if (databaseType != null) databaseIconLabel.setIcon(databaseType.getLargeIcon());
                    }
                }
            }.start();
        }

    };

    private EnvironmentConfigLocalListener environmentConfigListener = new EnvironmentConfigLocalListener() {
        @Override
        public void settingsChanged(EnvironmentTypeBundle environmentTypes) {

        }
    };
}
