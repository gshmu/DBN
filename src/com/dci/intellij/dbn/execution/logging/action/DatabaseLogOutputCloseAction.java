package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;
import static com.dci.intellij.dbn.common.util.ActionUtil.ensureProject;

public class DatabaseLogOutputCloseAction extends AbstractDatabaseLogOutputAction {
    public DatabaseLogOutputCloseAction() {
        super("Close", Icons.EXEC_RESULT_CLOSE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (Failsafe.check(loggingResult)) {
            if (loggingResult.getContext().isActive()) {
                MessageUtil.showQuestionDialog(
                        project,
                        "Process active",
                        "The process is still active. Closing the log output will interrupt the process. \nAre you sure you want to close the console?",
                        MessageUtil.OPTIONS_YES_NO, 0,
                        (option) -> conditional(option == 0,
                                () -> closeConsole(loggingResult, project)));
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
