package com.dci.intellij.dbn.execution.logging.action;

import static com.dci.intellij.dbn.common.util.ActionUtil.getProject;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.MessageCallback;
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
        final Project project = getProject(e);
        final DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (project != null && loggingResult != null && !loggingResult.isDisposed()) {
            final LogOutputContext context = loggingResult.getContext();
            if (context.isActive()) {
                MessageUtil.showQuestionDialog(
                        project,
                        "Kill process",
                        "This will interrupt the script execution process. \nAre you sure you want to continue?",
                        MessageUtil.OPTIONS_YES_NO, 0,
                        MessageCallback.create(0, () -> context.stop()));
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
