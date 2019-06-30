package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
        try {
            Project project = CommonUtil.nvln(
                    getProject(),
                    () -> Lookup.getProject(e));
            if (Failsafe.check(project)) {
                actionPerformed(e, project);
            }
        } catch (ProcessCanceledException ignore) {}
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
        try {
            Project project = Lookup.getProject(e);
            if (Failsafe.check(project)) {
                update(e, project);
            }
        } catch (ProcessCanceledException ignore){}
    }

    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
    };

    protected abstract void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project);
}
