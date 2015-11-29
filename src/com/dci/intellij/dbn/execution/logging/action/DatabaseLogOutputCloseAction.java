package com.dci.intellij.dbn.execution.logging.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
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
        final Project project = getEventProject(e);
        final DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (project != null && loggingResult != null && !loggingResult.isDisposed()) {
            if (loggingResult.getContext().isActive()) {
                SimpleTask closeConsoleTask = new SimpleTask() {
                    @Override
                    protected void execute() {
                        if (getOption() == 0) {
                            closeConsole(loggingResult, project);
                        }
                    }
                };
                MessageUtil.showQuestionDialog(
                        project,
                        "Process Active",
                        "The process is still active. Closing the log output will interrupt the process. \nAre you sure you want to close the console?",
                        MessageUtil.OPTIONS_YES_NO, 0, closeConsoleTask);
            } else {
                closeConsole(loggingResult, project);
            }

        }
    }

    private void closeConsole(DatabaseLoggingResult loggingResult, Project project) {
        loggingResult.getContext().close();
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.removeResultTab(loggingResult);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Close");
    }
}
