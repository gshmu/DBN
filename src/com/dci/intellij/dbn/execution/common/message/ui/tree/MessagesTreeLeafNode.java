package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public abstract class MessagesTreeLeafNode<P extends MessagesTreeNode, M extends ConsoleMessage> extends MessagesTreeNodeBase<P, MessagesTreeNode>{
    private M message;

    protected MessagesTreeLeafNode(P parent, M message) {
        super(parent);
        this.message = message;
    }

    @NotNull
    public final M getMessage() {
        return Failsafe.get(message);
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    @Override
    public final TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public final int getChildCount() {
        return 0;
    }

    @Override
    public final int getIndex(TreeNode node) {
        return -1;
    }

    @Override
    public final boolean getAllowsChildren() {
        return false;
    }

    @Override
    public final boolean isLeaf() {
        return true;
    }

    @Override
    public final Enumeration children() {
        return null;
    }


    @Override
    public void disposeInner() {
        Disposer.dispose(message);
        super.disposeInner();
    }
}
