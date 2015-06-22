package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.util.Disposer;

public class ConnectionFilterSettingsDialogForm extends DBNFormImpl<ConnectionFilterSettingsDialog>{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel contentPanel;
    private ConnectionFilterSettingsForm configurationEditor;

    public ConnectionFilterSettingsDialogForm(@NotNull ConnectionFilterSettingsDialog parentComponent, @NotNull ConnectionHandler connectionHandler) {
        super(parentComponent);
        DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(getProject());
        ConnectionSettings connectionSettings = settingsManager.getConnectionSettings().getConnectionSettings(connectionHandler.getId());
        configurationEditor = connectionSettings.getFilterSettings().createConfigurationEditor();
        contentPanel.add(configurationEditor.getComponent(), BorderLayout.CENTER);
        Disposer.register(this, configurationEditor);
    }

    public ConnectionFilterSettingsForm getConfigurationEditor() {
        return configurationEditor;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

}
