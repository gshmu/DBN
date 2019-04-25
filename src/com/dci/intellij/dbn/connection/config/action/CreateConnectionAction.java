package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CreateConnectionAction extends ConnectionSettingsAction {
    private DatabaseType databaseType;

    public CreateConnectionAction(@Nullable DatabaseType databaseType) {
        super(getName(databaseType), getIcon(databaseType));
        this.databaseType = databaseType;
    }

    private static Icon getIcon(@Nullable DatabaseType databaseType) {
        return databaseType == null ? null : databaseType.getIcon();
    }

    private static String getName(@Nullable DatabaseType databaseType) {
        return databaseType == null ? "Custom..." : databaseType.getDisplayName();
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionBundleSettingsForm settingsEditor = getSettingsForm(e);
        if (settingsEditor != null) {
            DatabaseType databaseType = this.databaseType;
            ConnectionConfigType configType = ConnectionConfigType.BASIC;
            if (databaseType == null) {
                configType = ConnectionConfigType.CUSTOM;
                databaseType = DatabaseType.UNKNOWN;
            }

            settingsEditor.createNewConnection(databaseType, configType);
        }
    }
}
