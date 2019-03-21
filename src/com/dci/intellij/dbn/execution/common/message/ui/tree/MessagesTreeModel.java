package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;


public class MessagesTreeModel extends DisposableBase implements TreeModel, Disposable {
    private Set<TreeModelListener> treeModelListeners = new HashSet<>();
    private MessagesTreeRootNode rootNode = new MessagesTreeRootNode(this);

    MessagesTreeModel() {
        Disposer.register(this, rootNode);
    }

    TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        return rootNode.addExecutionMessage(executionMessage);
    }

    TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        return rootNode.addCompilerMessage(compilerMessage);
    }

    TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage) {
        return rootNode.addExplainPlanMessage(explainPlanMessage);
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        return rootNode.getTreePath(compilerMessage);
    }

    @Nullable
    public TreePath getTreePath(StatementExecutionMessage statementExecutionMessage) {
        return rootNode.getTreePath(statementExecutionMessage);
    }


    public void notifyTreeModelListeners(TreePath treePath, TreeEventType eventType) {
        TreeUtil.notifyTreeModelListeners(this, treeModelListeners, treePath, eventType);
    }
    public void notifyTreeModelListeners(TreeNode node, TreeEventType eventType) {
        TreePath treePath = TreeUtil.createTreePath(node);
        notifyTreeModelListeners(treePath, eventType);
    }

    @Override
    public void disposeInner() {
        treeModelListeners.clear();
        rootNode = new MessagesTreeRootNode(this);
    }

   /*********************************************************
    *                       TreeModel                      *
    *********************************************************/
    @Override
    public Object getRoot() {
        return rootNode;
    }

    @Override
    public Object getChild(Object o, int i) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.getChildAt(i);
    }

    @Override
    public int getChildCount(Object o) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.getChildCount();
    }

    @Override
    public boolean isLeaf(Object o) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        TreeNode treeNode = (TreeNode) o;
        TreeNode childTreeNode = (TreeNode) o1;
        return treeNode.getIndex(childTreeNode);
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener) {
        treeModelListeners.add(treeModelListener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener) {
        treeModelListeners.remove(treeModelListener);
    }

    void resetMessagesStatus() {
        resetMessagesStatus(rootNode);
    }

    private void resetMessagesStatus(TreeNode node) {
        if (node instanceof MessagesTreeLeafNode) {
            MessagesTreeLeafNode messageTreeNode = (MessagesTreeLeafNode) node;
            messageTreeNode.getMessage().setNew(false);
        } else {
            Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements()) {
                TreeNode treeNode = children.nextElement();
                resetMessagesStatus(treeNode);
            }
        }
    }
}
