package com.dci.intellij.dbn.browser.action;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.action.ConnectionSettingsAction;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class NewConnectionAction extends ConnectionSettingsAction {
    private DatabaseType databaseType;

    public NewConnectionAction(@Nullable DatabaseType databaseType) {
        super(getName(databaseType), getIcon(databaseType));
        this.databaseType = databaseType;
    }

    private static Icon getIcon(@Nullable DatabaseType databaseType) {
        return databaseType == null ? null : databaseType.getIcon();
    }

    private static String getName(@Nullable DatabaseType databaseType) {
        return databaseType == null ? "Custom" : databaseType.getDisplayName();
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            DatabaseType databaseType = this.databaseType;
            ConnectionConfigType configType = ConnectionConfigType.BASIC;
            if (databaseType == null) {
                configType = ConnectionConfigType.CUSTOM;
                databaseType = DatabaseType.UNKNOWN;
            }
            settingsManager.createConnection(databaseType, configType);
        }
    }
}
