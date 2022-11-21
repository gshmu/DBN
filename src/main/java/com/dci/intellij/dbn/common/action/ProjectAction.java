package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Guarded;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isValid;

public abstract class ProjectAction extends AnAction {

    public ProjectAction() {
    }

    public ProjectAction(@Nullable String text) {
        super(text);
    }

    public ProjectAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public final void actionPerformed(@NotNull AnActionEvent e) {
        Guarded.run(() -> {
            Project project = Commons.coalesce(
                    () -> getProject(),
                    () -> Lookups.getProject(e));

            if (isNotValid(project)) return;
            actionPerformed(e, project);
        });
    }

    /**
     * fallback when project cannot be loaded from the data context (TODO check why)
     */
    @Nullable
    protected Project getProject() {
        return null;
    }

    @Override
    public final void update(@NotNull AnActionEvent e) {
        Guarded.run(() -> {
            Project project = Lookups.getProject(e);
            if (isValid(project)) update(e, project);
        });
    }

    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
    }

    protected abstract void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project);
}

