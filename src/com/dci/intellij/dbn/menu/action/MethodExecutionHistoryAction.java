package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MethodExecutionHistoryAction extends DumbAwareProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        executionManager.showExecutionHistoryDialog(null, true, false, null);
    }
}
