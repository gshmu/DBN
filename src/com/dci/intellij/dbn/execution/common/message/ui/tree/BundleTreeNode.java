package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;

public abstract class BundleTreeNode implements MessagesTreeNode{
    protected MessagesTreeNode parent;
    protected List<MessagesTreeNode> children = new ArrayList<MessagesTreeNode>();

    protected BundleTreeNode(MessagesTreeNode parent) {
        this.parent = parent;
    }

    public void dispose() {
        for (MessagesTreeNode treeNode : children) {
            treeNode.dispose();
        }
    }

    public MessagesTreeModel getTreeModel() {
        return parent.getTreeModel();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }
}
