package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

abstract class ExecutionMessagesAction extends DumbAwareProjectAction {
    private WeakRef<MessagesTree> messagesTree;

    ExecutionMessagesAction(MessagesTree messagesTree, String text, Icon icon) {
        super(text, null, icon);
        this.messagesTree = WeakRef.from(messagesTree);
    }

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        MessagesTree messagesTree = getMessagesTree();
        if (Failsafe.check(messagesTree)) {
            actionPerformed(e, project, messagesTree);
        }
    }

    @Override
    protected final void update(@NotNull AnActionEvent e, @NotNull Project project) {
        update(e, project, getMessagesTree());
    }

    protected abstract void update(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @Nullable MessagesTree messagesTree);

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull MessagesTree messagesTree);

    @Nullable
    private MessagesTree getMessagesTree() {
        return WeakRef.get(messagesTree);
    }
}
