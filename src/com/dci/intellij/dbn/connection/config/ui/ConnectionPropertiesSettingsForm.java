package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.event.EventNotifier;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.properties.ui.PropertiesEditorForm;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionPropertiesSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ConnectionPropertiesSettingsForm extends ConfigurationEditorForm<ConnectionPropertiesSettings> {
    private JPanel mainPanel;
    private JCheckBox autoCommitCheckBox;
    private JPanel propertiesPanel;

    private PropertiesEditorForm propertiesEditorForm;

    public ConnectionPropertiesSettingsForm(final ConnectionPropertiesSettings configuration) {
        super(configuration);

        Map<String, String> properties = new HashMap<>(configuration.getProperties());

        propertiesEditorForm = new PropertiesEditorForm(this, properties, true);
        propertiesPanel.add(propertiesEditorForm.getComponent(), BorderLayout.CENTER);


        resetFormChanges();
        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        ConnectionPropertiesSettings configuration = getConfiguration();
        boolean newAutoCommit = autoCommitCheckBox.isSelected();
        boolean settingsChanged = configuration.isEnableAutoCommit() != newAutoCommit;

        applyFormChanges(configuration);

        Project project = configuration.getProject();
        ConnectionId connectionId = configuration.getConnectionId();
        SettingsChangeNotifier.register(() -> {
            if (settingsChanged) {
                EventNotifier.notify(project,
                        ConnectionHandlerStatusListener.TOPIC,
                        (listener) -> listener.statusChanged(connectionId));
            }
        });
    }

    @Override
    public void applyFormChanges(ConnectionPropertiesSettings configuration) throws ConfigurationException {
        propertiesEditorForm.getTable().stopCellEditing();
        configuration.setEnableAutoCommit(autoCommitCheckBox.isSelected());
        configuration.setProperties(propertiesEditorForm.getProperties());
    }

    @Override
    public void resetFormChanges() {
        ConnectionPropertiesSettings configuration = getConfiguration();
        autoCommitCheckBox.setSelected(configuration.isEnableAutoCommit());
        propertiesEditorForm.setProperties(configuration.getProperties());
    }


    @Override
    public void disposeInner() {
        Disposer.dispose(propertiesEditorForm);
        super.disposeInner();
    }
}
