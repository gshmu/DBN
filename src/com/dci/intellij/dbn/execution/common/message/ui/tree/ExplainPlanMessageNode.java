package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class ExplainPlanMessageNode implements MessageTreeNode {
    private ExplainPlanMessage explainPlanMessage;
    private ExplainPlanMessagesFileNode parent;

    public ExplainPlanMessageNode(ExplainPlanMessagesFileNode parent, ExplainPlanMessage explainPlanMessage) {
        this.parent = parent;
        this.explainPlanMessage = explainPlanMessage;

        Disposer.register(this, explainPlanMessage);
    }

    public ExplainPlanMessage getExplainPlanMessage() {
        return FailsafeUtil.get(explainPlanMessage);
    }

    public VirtualFile getVirtualFile() {
        return getParent().getVirtualFile();
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

    public ExplainPlanMessagesFileNode getParent() {
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
        ExplainPlanMessage explainPlanMessage = getExplainPlanMessage();
        return
            explainPlanMessage.getText() + " - Connection: " +
            explainPlanMessage.getConnectionHandler().getName();
    }

    @NotNull
    @Override
    public ConsoleMessage getMessage() {
        return getExplainPlanMessage();
    }

    public void dispose() {
        explainPlanMessage = null;
        parent = null;
    }
}
