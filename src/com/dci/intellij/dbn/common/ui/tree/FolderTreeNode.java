package com.dci.intellij.dbn.common.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class FolderTreeNode extends LeafTreeNode{
    List<TreeNode> children = new ArrayList<TreeNode>();

    public FolderTreeNode(TreeNode parent, Object userObject, List<TreeNode> children) {
        super(parent, userObject);
        this.children = children;
    }

    public void addChild(TreeNode child) {
        children.add(child);           
    }

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
        return false;
    }

    @Override
    public Enumeration children() {
        final Iterator iterator = children.iterator();
        return new Enumeration() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public Object nextElement() {
                return iterator.next();
            }
        };
    }
}
