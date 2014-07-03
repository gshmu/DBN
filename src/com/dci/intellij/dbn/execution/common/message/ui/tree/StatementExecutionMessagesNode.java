package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.TreePath;

public class StatementExecutionMessagesNode extends BundleTreeNode {
    public StatementExecutionMessagesNode(RootNode parent) {
        super(parent);
    }

    public MessagesTreeNode getChildTreeNode(VirtualFile virtualFile) {
        for (MessagesTreeNode messagesTreeNode : getChildren()) {
            if (messagesTreeNode.getVirtualFile().equals(virtualFile)) {
                return messagesTreeNode;
            }
        }
        return null;
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        StatementExecutionMessagesFileNode node = (StatementExecutionMessagesFileNode) getChildTreeNode(executionMessage.getVirtualFile());
        if (node == null) {
            node = new StatementExecutionMessagesFileNode(this, executionMessage.getVirtualFile());
            addChild(node);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return node.addExecutionMessage(executionMessage);
    }

    public VirtualFile getVirtualFile() {
        return null;
    }
}
