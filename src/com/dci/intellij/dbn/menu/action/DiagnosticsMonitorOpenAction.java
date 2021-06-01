package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DiagnosticsMonitorOpenAction extends DumbAwareProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DiagnosticsManager executionManager = DiagnosticsManager.getInstance(project);
        executionManager.openDiagnosticsMonitorDialog();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        super.update(e, project);
        e.getPresentation().setVisible(DatabaseNavigator.DEVELOPER);
    }
}
