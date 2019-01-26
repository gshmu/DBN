package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class ExplainPlanMessageNode extends DisposableBase implements MessageTreeNode {
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

    @Override
    public VirtualFile getVirtualFile() {
        return getParent().getVirtualFile();
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
    public ExplainPlanMessagesFileNode getParent() {
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

    @Override
    public void dispose() {
        super.dispose();
        explainPlanMessage = null;
        parent = null;
    }
}
