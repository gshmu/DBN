package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractDataEditorAction extends DumbAwareProjectAction {
    public AbstractDataEditorAction(String text) {
        super(text);
    }

    public AbstractDataEditorAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatasetEditor datasetEditor = DatasetEditor.get(e);
        if (Failsafe.check(datasetEditor)) {
            actionPerformed(e, project, datasetEditor);
        }
    }

    @Override
    protected final void update(@NotNull AnActionEvent e, @NotNull Project project) {
        update(e, project, DatasetEditor.get(e));
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DatasetEditor datasetEditor);

    protected abstract void update(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @Nullable DatasetEditor datasetEditor);
}
