package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.action.ProjectAction;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DiagnosticsSettingsOpenAction extends ProjectAction implements DumbAware {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(project);
        diagnosticsManager.openDiagnosticsSettingsDialog();

    }

    @Override
    public void update(@NotNull AnActionEvent e, @NotNull Project project) {
        //e.getPresentation().setVisible(Diagnostics.developerMode);
    }
}
