package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExpandMessagesTreeAction extends ExecutionMessagesAction {

    public ExpandMessagesTreeAction(MessagesTree messagesTree) {
        super(messagesTree, "Expand All", Icons.ACTION_EXPAND_ALL);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull MessagesTree messagesTree) {
        TreeUtil.expandAll(messagesTree);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable MessagesTree messagesTree) {

    }
}