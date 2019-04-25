package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloseMessagesWindowAction extends ExecutionMessagesAction {
    public CloseMessagesWindowAction(MessagesTree messagesTree) {
        super(messagesTree, "Close", Icons.EXEC_RESULT_CLOSE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull MessagesTree messagesTree) {
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.removeMessagesTab();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable MessagesTree messagesTree) {

    }
}
