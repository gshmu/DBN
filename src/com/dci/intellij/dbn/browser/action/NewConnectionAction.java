package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.action.ConnectionSettingsAction;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class NewConnectionAction extends ConnectionSettingsAction {
    private DatabaseType databaseType;

    public NewConnectionAction(@NotNull DatabaseType databaseType) {
        super(databaseType.getDisplayName(), databaseType.getIcon());
        this.databaseType = databaseType;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            settingsManager.createConnection(databaseType);
        }
    }
}
