package com.dci.intellij.dbn.common.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class LeafTreeNode implements TreeNode {
    private TreeNode parent;
    private Object userObject;

    public LeafTreeNode(TreeNode parent, Object userObject) {
        this.parent = parent;
        this.userObject = userObject;
    }

    public Object getUserObject() {
        return userObject;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public TreeNode getParent() {
        return parent;
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
}
