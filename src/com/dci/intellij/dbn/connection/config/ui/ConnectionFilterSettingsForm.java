package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.options.ConfigurationException;

public class ConnectionFilterSettingsForm extends CompositeConfigurationEditorForm<ConnectionFilterSettings>{
    private JPanel mainPanel;
    private JPanel objectTypesFilterPanel;
    private JPanel objectNameFiltersPanel;
    private JCheckBox hideEmptySchemasCheckBox;

    public ConnectionFilterSettingsForm(ConnectionFilterSettings settings) {
        super(settings);
        objectTypesFilterPanel.add(settings.getObjectTypeFilterSettings().createComponent(), BorderLayout.CENTER);
        objectNameFiltersPanel.add(settings.getObjectNameFilterSettings().createComponent(), BorderLayout.CENTER);

        hideEmptySchemasCheckBox.setSelected(settings.isHideEmptySchemas());
        registerComponent(hideEmptySchemasCheckBox);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void applyChanges() throws ConfigurationException {
        super.applyChanges();
        ConnectionFilterSettings configuration = getConfiguration();
        boolean notifyFilterListeners = configuration.isHideEmptySchemas() != hideEmptySchemasCheckBox.isSelected();
        configuration.setHideEmptySchemas(hideEmptySchemasCheckBox.isSelected());
        if (notifyFilterListeners) {
            ObjectFilterChangeListener listener = EventManager.notify(getConfiguration().getProject(), ObjectFilterChangeListener.TOPIC);
            ConnectionHandler connectionHandler = getConnectionHandler();
            listener.nameFiltersChanged(connectionHandler, DBObjectType.SCHEMA);
        }
    }

    private ConnectionHandler getConnectionHandler() {
        ConnectionFilterSettings configuration = getConfiguration();
        ConnectionManager connectionManager = ConnectionManager.getInstance(configuration.getProject());
        for (ConnectionBundle connectionBundle : connectionManager.getConnectionBundles()) {
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                if (configuration == connectionHandler.getSettings().getFilterSettings()) {
                    return connectionHandler;
                }
            }
        }
        return null;
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
