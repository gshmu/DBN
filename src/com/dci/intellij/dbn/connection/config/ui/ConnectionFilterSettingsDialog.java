package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.ui.DBNContentWithHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectionFilterSettingsDialog extends DBNDialog<DBNContentWithHeaderForm<ConnectionFilterSettingsDialog>> {
    private ConnectionFilterSettingsForm configurationEditor;
    private ConnectionHandlerRef connectionHandlerRef;
    public ConnectionFilterSettingsDialog(@NotNull final ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Object filters", true);
        connectionHandlerRef = connectionHandler.getRef();
        setModal(true);
        setResizable(true);
        init();
    }

    @NotNull
    @Override
    protected DBNContentWithHeaderForm<ConnectionFilterSettingsDialog> createComponent() {
        final ConnectionHandler connectionHandler = connectionHandlerRef.getnn();
        return new DBNContentWithHeaderForm<ConnectionFilterSettingsDialog>(this) {
            @Override
            public DBNHeaderForm createHeaderForm() {
                return new DBNHeaderForm(connectionHandler, this);
            }

            @Override
            public DBNForm createContentForm() {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(getProject());
                ConnectionSettings connectionSettings = settingsManager.getConnectionSettings().getConnectionSettings(connectionHandler.getId());
                configurationEditor = connectionSettings.getFilterSettings().createConfigurationEditor();
                return configurationEditor;
            }
        };
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
            ConnectionFilterSettings configuration = configurationEditor.getConfiguration();
            // !!workaround!! apply settings is normally cascaded from top level settings
            configurationEditor.applyFormChanges();
            configuration.apply();
            configuration.notifyChanges();
            super.doOKAction();
        } catch (ConfigurationException e) {
            MessageUtil.showErrorDialog(getProject(), "Configuration error", e.getMessage());
        }

    }

    public void doCancelAction() {
        super.doCancelAction();
    }

}
