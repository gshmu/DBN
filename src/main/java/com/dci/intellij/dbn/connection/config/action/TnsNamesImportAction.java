package com.dci.intellij.dbn.connection.config.action;


import com.dci.intellij.dbn.connection.config.tns.TnsImportService;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TnsNamesImportAction extends ConnectionSettingsAction{
    TnsNamesImportAction() {
        super("Import TNS Names", null);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ConnectionBundleSettingsForm target) {
        TnsImportService importService = TnsImportService.getInstance();
        importService.importTnsNames(project, d -> target.importTnsNames(d));
    }
}
