package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.options.ConfigurationHandle;
import com.dci.intellij.dbn.common.ui.DBNContentWithHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;

public class ConnectionFilterSettingsDialog extends DBNDialog<DBNContentWithHeaderForm> {
    private final ConnectionHandlerRef connectionHandler;
    private ConnectionFilterSettingsForm configurationEditor;
    private ConnectionFilterSettings filterSettings;

    public ConnectionFilterSettingsDialog(@NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Object filters", true);
        this.connectionHandler = connectionHandler.getRef();
        setModal(true);
        setResizable(true);
        init();
    }

    @NotNull
    @Override
    protected DBNContentWithHeaderForm createForm() {
        ConnectionHandler connectionHandler = this.connectionHandler.ensure();
        return new DBNContentWithHeaderForm(this) {
            @Override
            public DBNHeaderForm createHeaderForm() {
                return new DBNHeaderForm(this, connectionHandler);
            }

            @Override
            public DBNForm createContentForm() {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(getProject());
                ConnectionSettings connectionSettings = settingsManager.getConnectionSettings().getConnectionSettings(connectionHandler.getConnectionId());
                filterSettings = connectionSettings.getFilterSettings();
                configurationEditor = filterSettings.createConfigurationEditor();
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

    @Override
    public void doOKAction() {
        try {
            // !!workaround!! apply settings is normally cascaded from top level settings
            configurationEditor.applyFormChanges();
            filterSettings.apply();
            ConfigurationHandle.notifyChanges();
            super.doOKAction();
        } catch (ConfigurationException e) {
            Messages.showErrorDialog(getProject(), "Configuration error", e.getMessage());
        }

    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    @Override
    protected void disposeInner() {
        filterSettings.disposeUIResources();
    }
}
