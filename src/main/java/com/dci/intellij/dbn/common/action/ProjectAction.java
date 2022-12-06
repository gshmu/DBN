package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.util.Commons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions.ActionText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.intellij.openapi.util.NlsActions.ActionDescription;

public abstract class ProjectAction extends BasicAction implements DumbAware {

    public ProjectAction() {}

    public ProjectAction(@Nullable @ActionText String text) {
        super(text);
    }

    public ProjectAction(@Nullable @ActionText String text, @Nullable @ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public final void actionPerformed(@NotNull AnActionEvent e) {
        guarded(() -> {
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
    public Project getProject() {
        return null;
    }

    @Override
    public final void update(@NotNull AnActionEvent e) {
        guarded(() -> {
            Project project = Lookups.getProject(e);
            if (isValid(project)) update(e, project);
        });
    }

    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
    }

    protected abstract void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project);





}

