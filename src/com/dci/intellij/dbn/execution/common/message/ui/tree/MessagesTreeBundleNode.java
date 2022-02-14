package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.CollectionUtil;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public abstract class MessagesTreeBundleNode<P extends MessagesTreeNode, C extends MessagesTreeNode>
        extends MessagesTreeNodeBase<P, C> {

    private final List<C> children = CollectionUtil.createConcurrentList();

    protected MessagesTreeBundleNode(P parent) {
        super(parent);
    }

    public void addChild(C child) {
        children.add(child);
    }

    protected void clearChildren() {
        List<MessagesTreeNode> children = new ArrayList<>(this.children);
        this.children.clear();
        SafeDisposer.dispose(children, true, true);
    }

    @Override
    public List<C> getChildren() {
        return children;
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return children.size() == 0;
    }

    @Override
    public Enumeration children() {
        return java.util.Collections.enumeration(children);
    }

    @Override
    public boolean hasMessageChildren(MessageType type) {
        for (C child : children) {
            if (child instanceof MessagesTreeLeafNode) {
                MessagesTreeLeafNode messageTreeNode = (MessagesTreeLeafNode) child;
                if (messageTreeNode.getMessage().getType() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    /*********************************************************
     *                      Disposable                       *
     ********************************************************  */
    @Override
    public void disposeInner() {
        SafeDisposer.dispose(children, true, true);
        nullify();
    }


}
