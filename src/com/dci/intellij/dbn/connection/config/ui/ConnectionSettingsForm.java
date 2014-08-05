package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.options.EnvironmentPresentationChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.ui.tab.TabbedPaneUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.intellij.ui.tabs.TabInfo;

public class ConnectionSettingsForm extends CompositeConfigurationEditorForm<ConnectionSettings>{
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private TabbedPane configTabbedPane;

    private DBNHeaderForm headerForm;

    public ConnectionSettingsForm(ConnectionSettings connectionSettings) {
        super(connectionSettings);
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        configTabbedPane = new TabbedPane(databaseSettings.getProject());
        contentPanel.add(configTabbedPane, BorderLayout.CENTER);


        TabInfo connectionTabInfo = new TabInfo(databaseSettings.createComponent());
        connectionTabInfo.setText("Connection");
        configTabbedPane.addTab(connectionTabInfo);

        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        TabInfo detailsTabInfo = new TabInfo(detailSettings.createComponent());
        detailsTabInfo.setText("Details");
        configTabbedPane.addTab(detailsTabInfo);

        ConnectionFilterSettings filterSettings = connectionSettings.getFilterSettings();
        TabInfo filtersTabInfo = new TabInfo(filterSettings.createComponent());
        filtersTabInfo.setText("Filters");
        configTabbedPane.addTab(filtersTabInfo);

        GenericDatabaseSettingsForm databaseSettingsForm = databaseSettings.getSettingsEditor();
        ConnectionDetailSettingsForm detailSettingsForm = detailSettings.getSettingsEditor();
        filterSettings.getSettingsEditor();

        headerForm = new DBNHeaderForm();
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        EventManager.subscribe(databaseSettings.getProject(), ConnectionPresentationChangeListener.TOPIC, connectionPresentationChangeListener);
        EventManager.subscribe(databaseSettings.getProject(), EnvironmentPresentationChangeListener.TOPIC, environmentPresentationChangeListener);

        databaseSettingsForm.notifyPresentationChanges();
        detailSettingsForm.notifyPresentationChanges();
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
        public void presentationChanged(String name, Icon icon, Color color, String connectionId, DatabaseType databaseType) {
            if (getConfiguration().getConnectionId().equals(connectionId)) {
                if (name != null) headerForm.setTitle(name);
                if (icon != null) headerForm.setIcon(icon);
                if (color != null) headerForm.setBackground(color);
                //if (databaseType != null) databaseIconLabel.setIcon(databaseType.getLargeIcon());
            }
        }

    };

    private EnvironmentPresentationChangeListener environmentPresentationChangeListener = new EnvironmentPresentationChangeListener() {
        @Override
        public void settingsChanged(EnvironmentTypeBundle environmentTypes) {

        }
    };


    @Override
    public void dispose() {
        super.dispose();
        EventManager.unsubscribe(connectionPresentationChangeListener, environmentPresentationChangeListener);
    }
}
