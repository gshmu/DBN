package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Set;


public class MessagesTreeModel implements TreeModel {
    private Set<TreeModelListener> treeModelListeners = new HashSet<TreeModelListener>();
    RootNode rootNode = new RootNode(this);

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        return rootNode.addExecutionMessage(executionMessage);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        return rootNode.addCompilerMessage(compilerMessage);
    }

    public TreePath getTreePath(CompilerMessage compilerMessage) {
        return rootNode.getTreePath(compilerMessage);
    }


    public void notifyTreeModelListeners(TreeNode node, TreeEventType eventType) {
        TreePath treePath = TreeUtil.createTreePath(node);
        TreeUtil.notifyTreeModelListeners(this, treeModelListeners, treePath, eventType);
    }

    public void invalidate() {
        rootNode.dispose();
    }

   /*********************************************************
    *                       TreeModel                      *
    *********************************************************/
    public Object getRoot() {
        return rootNode;
    }

    public Object getChild(Object o, int i) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.getChildAt(i);
    }

    public int getChildCount(Object o) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.getChildCount();
    }

    public boolean isLeaf(Object o) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.isLeaf();
    }

    public void valueForPathChanged(TreePath treePath, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIndexOfChild(Object o, Object o1) {
        TreeNode treeNode = (TreeNode) o;
        TreeNode childTreeNode = (TreeNode) o1;
        return treeNode.getIndex(childTreeNode);
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) {
        treeModelListeners.add(treeModelListener);
    }

    public void removeTreeModelListener(TreeModelListener treeModelListener) {
        treeModelListeners.remove(treeModelListener);
    }
}
