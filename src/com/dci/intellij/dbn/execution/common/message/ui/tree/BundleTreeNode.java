package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.intellij.openapi.util.Disposer;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public abstract class BundleTreeNode extends DisposableBase implements MessagesTreeNode{
    protected MessagesTreeNode parent;
    private List<MessagesTreeNode> children = CollectionUtil.createConcurrentList();

    protected BundleTreeNode(MessagesTreeNode parent) {
        this.parent = parent;
    }

    public void addChild(MessagesTreeNode child) {
        children.add(child);
        Disposer.register(this, child);
    }

    public void clearChildren() {
        List<MessagesTreeNode> children = new ArrayList<>(this.children);
        this.children.clear();
        DisposerUtil.dispose(children);
    }

    @Override
    public MessagesTreeModel getTreeModel() {
        return parent.getTreeModel();
    }

    public List<MessagesTreeNode> getChildren() {
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
    public TreeNode getParent() {
        return parent;
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

    /*********************************************************
     *                      Disposable                       *
     *********************************************************/
    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            CollectionUtil.clear(children);
            parent = null;
        }
    }


}
