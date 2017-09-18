package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNContentWithHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.options.ConfigurationException;

public class ConnectionFilterSettingsDialog extends DBNDialog<DBNContentWithHeaderForm<ConnectionFilterSettingsDialog>> {
    private ConnectionFilterSettingsForm configurationEditor;
    public ConnectionFilterSettingsDialog(@NotNull final ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Object Filters", true);
        component = new DBNContentWithHeaderForm<ConnectionFilterSettingsDialog>(this) {
            @Override
            public DBNHeaderForm createHeaderForm() {
                return new DBNHeaderForm(connectionHandler);
            }

            @Override
            public DBNForm createContentForm() {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(getProject());
                ConnectionSettings connectionSettings = settingsManager.getConnectionSettings().getConnectionSettings(connectionHandler.getId());
                configurationEditor = connectionSettings.getFilterSettings().createConfigurationEditor();
                return configurationEditor;
            }
        };
        setModal(true);
        setResizable(true);
        init();
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
