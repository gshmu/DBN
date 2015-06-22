package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.intellij.openapi.options.ConfigurationException;

public class ConnectionFilterSettingsDialog extends DBNDialog<ConnectionFilterSettingsDialogForm> {
    public ConnectionFilterSettingsDialog(@NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Object Filters", true);
        component = new ConnectionFilterSettingsDialogForm(this, connectionHandler);
        setModal(true);
        setResizable(true);
        init();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component == null ? null : component.getPreferredFocusedComponent();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction()
        };
    }

    public void doOKAction() {
        try {
            ConnectionFilterSettingsForm configurationEditor = component.getConfigurationEditor();
            ConnectionFilterSettings configuration = configurationEditor.getConfiguration();
            // !!workaround!! apply settings is normally cascaded from top level settings
            configurationEditor.applyFormChanges();
            configuration.apply();
            configuration.notifyChanges();
            super.doOKAction();
        } catch (ConfigurationException e) {
            MessageUtil.showErrorDialog(getProject(), "Configuration Error", e.getMessage());
        }

    }

    public void doCancelAction() {
        super.doCancelAction();
    }

}
