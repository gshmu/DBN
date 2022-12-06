package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.action.ProjectAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ResourceMonitorOpenAction extends ProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseTransactionManager executionManager = DatabaseTransactionManager.getInstance(project);
        executionManager.showResourceMonitorDialog();
    }
}
