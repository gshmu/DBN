package com.dci.intellij.dbn.execution.logging.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class DatabaseLogOutputCloseAction extends AbstractDatabaseLogOutputAction {
    public DatabaseLogOutputCloseAction() {
        super("Close", Icons.EXEC_RESULT_CLOSE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (project != null && loggingResult != null && !loggingResult.isDisposed()) {
            loggingResult.getContext().cancel();
            ExecutionManager executionManager = ExecutionManager.getInstance(project);
            executionManager.removeResultTab(loggingResult);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Close");
    }
}
