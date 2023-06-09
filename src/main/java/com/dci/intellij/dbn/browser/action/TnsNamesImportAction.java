package com.dci.intellij.dbn.browser.action;


import com.dci.intellij.dbn.common.action.ProjectAction;
import com.dci.intellij.dbn.connection.config.tns.TnsImportService;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TnsNamesImportAction extends ProjectAction {
    TnsNamesImportAction() {
        super("Import TNS Names", null, null);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        TnsImportService importService = TnsImportService.getInstance();
        importService.importTnsNames(project, d -> settingsManager.createConnections(d));
    }
}
