package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExpandMessagesTreeAction extends ExecutionMessagesAction {

    public ExpandMessagesTreeAction(MessagesTree messagesTree) {
        super(messagesTree, "Expand All", Icons.ACTION_EXPAND_ALL);
    }

    public void actionPerformed(AnActionEvent e) {
        TreeUtil.expandAll(getMessagesTree());
    }
}