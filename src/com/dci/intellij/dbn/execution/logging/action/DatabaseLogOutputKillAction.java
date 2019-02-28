package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.component.ComponentBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;
import static com.dci.intellij.dbn.common.util.ActionUtil.ensureProject;

public class DatabaseLogOutputKillAction extends AbstractDatabaseLogOutputAction implements ComponentBase {
    public DatabaseLogOutputKillAction() {
        super("Kill Process", Icons.KILL_PROCESS);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (Failsafe.check(loggingResult)) {
            final LogOutputContext context = loggingResult.getContext();
            if (context.isActive()) {
                MessageUtil.showQuestionDialog(
                        project,
                        "Kill process",
                        "This will interrupt the script execution process. \nAre you sure you want to continue?",
                        MessageUtil.OPTIONS_YES_NO, 0,
                        (option) -> conditional(option == 0,
                                () -> context.stop()));

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
