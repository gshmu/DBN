package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.properties.ui.PropertiesEditorForm;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class ConnectionPropertiesSettingsForm extends ConfigurationEditorForm<ConnectionPropertiesSettings>{
    private JPanel mainPanel;
    private JCheckBox autoCommitCheckBox;
    private JPanel propertiesPanel;

    private PropertiesEditorForm propertiesEditorForm;

    public ConnectionPropertiesSettingsForm(final ConnectionPropertiesSettings configuration) {
        super(configuration);

        Map<String, String> properties = new HashMap<String, String>();
        properties.putAll(configuration.getProperties());

        propertiesEditorForm = new PropertiesEditorForm(this, properties, true);
        propertiesPanel.add(propertiesEditorForm.getComponent(), BorderLayout.CENTER);


        resetFormChanges();
        registerComponent(mainPanel);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        final ConnectionPropertiesSettings configuration = getConfiguration();
        boolean newAutoCommit = autoCommitCheckBox.isSelected();
        final boolean settingsChanged = configuration.isEnableAutoCommit() != newAutoCommit;

        configuration.setEnableAutoCommit(newAutoCommit);
        configuration.setProperties(propertiesEditorForm.getProperties());


        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                Project project = configuration.getProject();
                if (settingsChanged) {
                    ConnectionStatusListener listener = EventUtil.notify(project, ConnectionStatusListener.TOPIC);
                    listener.statusChanged(configuration.getConnectionId());
                }
            }
        };
    }

    @Override
    public void resetFormChanges() {
        ConnectionPropertiesSettings configuration = getConfiguration();
        autoCommitCheckBox.setSelected(configuration.isEnableAutoCommit());
        propertiesEditorForm.setProperties(configuration.getProperties());
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
