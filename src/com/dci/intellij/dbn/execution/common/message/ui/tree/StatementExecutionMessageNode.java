package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class StatementExecutionMessageNode extends DisposableBase implements MessageTreeNode {
    private StatementExecutionMessage executionMessage;
    private StatementExecutionMessagesFileNode parent;

    public StatementExecutionMessageNode(StatementExecutionMessagesFileNode parent, StatementExecutionMessage executionMessage) {
        this.parent = parent;
        this.executionMessage = executionMessage;

        Disposer.register(this, executionMessage);
    }

    @NotNull
    public StatementExecutionMessage getExecutionMessage() {
        return FailsafeUtil.get(executionMessage);
    }

    public VirtualFile getVirtualFile() {
        return parent.getVirtualFile();
    }

    public MessagesTreeModel getTreeModel() {
        return getParent().getTreeModel();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    public int getChildCount() {
        return 0;
    }

    public StatementExecutionMessagesFileNode getParent() {
        return FailsafeUtil.get(parent);
    }

    public int getIndex(TreeNode node) {
        return -1;
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration children() {
        return null;
    }

    @Override
    public String toString() {
        StatementExecutionMessage executionMessage = getExecutionMessage();
        return
            executionMessage.getText() + " " +
            executionMessage.getCauseMessage() + " - Connection: " +
            executionMessage.getExecutionResult().getConnectionHandler().getName() + ": " +
            executionMessage.getExecutionResult().getExecutionDuration() + "ms";
    }

    @NotNull
    @Override
    public ConsoleMessage getMessage() {
        return getExecutionMessage();
    }

    public void dispose() {
        super.dispose();
        executionMessage = null;
        parent = null;
    }
}
