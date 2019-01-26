package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

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

    @Override
    public VirtualFile getVirtualFile() {
        return parent.getVirtualFile();
    }

    @Override
    public MessagesTreeModel getTreeModel() {
        return getParent().getTreeModel();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    @Override
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public StatementExecutionMessagesFileNode getParent() {
        return FailsafeUtil.get(parent);
    }

    @Override
    public int getIndex(TreeNode node) {
        return -1;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
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

    @Override
    public void dispose() {
        super.dispose();
        executionMessage = null;
        parent = null;
    }
}
