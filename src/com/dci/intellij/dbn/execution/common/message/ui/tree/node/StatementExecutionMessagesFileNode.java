package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeBundleNode;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;

public class StatementExecutionMessagesFileNode extends MessagesTreeBundleNode<StatementExecutionMessagesNode, StatementExecutionMessageNode> {
    private VirtualFile virtualFile;

    StatementExecutionMessagesFileNode(StatementExecutionMessagesNode parent, VirtualFile virtualFile) {
        super(parent);
        this.virtualFile = virtualFile;
    }

    @NotNull
    @Override
    public VirtualFile getVirtualFile() {
        return Failsafe.get(virtualFile);
    }

    TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        StatementExecutionMessageNode execMessageNode = new StatementExecutionMessageNode(this, executionMessage);
        addChild(execMessageNode);
        getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        return TreeUtil.createTreePath(execMessageNode);
    }

    @Nullable
    public TreePath getTreePath(StatementExecutionMessage executionMessage) {
        for (StatementExecutionMessageNode messageNode : getChildren()) {
            if (messageNode.getMessage() == executionMessage) {
                return TreeUtil.createTreePath(messageNode);
            }
        }
        return null;
    }
}
