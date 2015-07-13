package com.dci.intellij.dbn.execution.logging.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class DatabaseLogOutputKillAction extends AbstractDatabaseLogOutputAction {
    public DatabaseLogOutputKillAction() {
        super("Kill Process", Icons.KILL_PROCESS);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = getEventProject(e);
        final DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (project != null && loggingResult != null && !loggingResult.isDisposed()) {
            final LogOutputContext context = loggingResult.getContext();
            if (context.isActive()) {
                SimpleTask killConsoleTask = new SimpleTask() {
                    @Override
                    protected void execute() {
                        if (getHandle() == 0) {
                            context.stop();
                        }
                    }
                };
                MessageUtil.showQuestionDialog(
                        project,
                        "Kill Process",
                        "This will forcibly interrupt the process. \nAre you sure you want to continue?",
                        MessageUtil.OPTIONS_YES_NO, 0, killConsoleTask);
            } else {
                context.stop();
            }

        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Kill Process");

        DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        LogOutputContext context = loggingResult == null ? null : loggingResult.getContext();
        boolean enabled = context != null && context.isActive();
        boolean visible = context != null && context.getProcess() != null;
        presentation.setEnabled(enabled);
        presentation.setVisible(visible);

    }
}
