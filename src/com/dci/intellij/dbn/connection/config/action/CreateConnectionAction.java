package com.dci.intellij.dbn.connection.config.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreateConnectionAction extends ConnectionSettingsAction {
    private DatabaseType databaseType;

    public CreateConnectionAction(@NotNull DatabaseType databaseType) {
        super(databaseType.getDisplayName(), databaseType.getIcon());
        this.databaseType = databaseType;
    }

    public void actionPerformed(AnActionEvent e) {
        ConnectionBundleSettingsForm settingsEditor = getSettingsForm(e);
        if (settingsEditor != null) {
            settingsEditor.createNewConnection(databaseType);
        }
    }
}
