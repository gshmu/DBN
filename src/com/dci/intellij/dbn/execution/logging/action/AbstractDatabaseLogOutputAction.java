package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractDatabaseLogOutputAction extends DumbAwareProjectAction {
    AbstractDatabaseLogOutputAction(String text, Icon icon) {
        super(text, null, icon);
    }

    private static DatabaseLoggingResult getLoggingResult(AnActionEvent e) {
        return e.getData(DataKeys.DATABASE_LOG_OUTPUT);
    }

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseLoggingResult loggingResult = getLoggingResult(e);
        if (Failsafe.check(loggingResult)) {
            actionPerformed(e, project, loggingResult);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseLoggingResult loggingResult = getLoggingResult(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(loggingResult != null);
        update(e, project, loggingResult);
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DatabaseLoggingResult loggingResult);

    protected abstract void update(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @Nullable DatabaseLoggingResult loggingResult);

}
