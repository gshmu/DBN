package com.dci.intellij.dbn.common.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.dispose.Checks.isValid;

public abstract class DumbAwareContextAction<T> extends DumbAwareProjectAction implements DumbAware {

    public DumbAwareContextAction() {}

    public DumbAwareContextAction(@Nullable String text) {
        super(text);
    }

    public DumbAwareContextAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    protected abstract T getTarget(@NotNull AnActionEvent e);

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        T context = getTarget(e);
        if (isValid(context)) {
            actionPerformed(e, project, context);
        }
    }

    @Override
    protected final void update(@NotNull AnActionEvent e, @NotNull Project project) {
        T target = getTarget(e);
        boolean enabled = isValid(target);

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        update(e, presentation, project, target);
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull T target);

    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable T target){};

}
