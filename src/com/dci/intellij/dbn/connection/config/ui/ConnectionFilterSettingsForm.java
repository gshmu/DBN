package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.EventUtil;
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
    public void applyFormChanges() throws ConfigurationException {
        final ConnectionFilterSettings configuration = getConfiguration();
        final boolean notifyFilterListeners = configuration.isHideEmptySchemas() != hideEmptySchemasCheckBox.isSelected();
        applyFormChanges(configuration);

        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (notifyFilterListeners) {
                    ObjectFilterChangeListener listener = EventUtil.notify(getConfiguration().getProject(), ObjectFilterChangeListener.TOPIC);
                    listener.nameFiltersChanged(DBObjectType.SCHEMA, configuration.getConnectionId());
                }
            }
        };
    }

    @Override
    public void applyFormChanges(ConnectionFilterSettings configuration) throws ConfigurationException {
        configuration.setHideEmptySchemas(hideEmptySchemasCheckBox.isSelected());
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
