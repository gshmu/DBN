package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PendingTransactionsAction extends DumbAwareProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseTransactionManager executionManager = DatabaseTransactionManager.getInstance(project);
        executionManager.showPendingTransactionsOverviewDialog(null);
    }
}
