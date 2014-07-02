package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.vfs.DatabaseContentFile;
import com.dci.intellij.dbn.execution.method.MethodExecutionMessage;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class MethodExecutionMessageNode implements MessagesTreeNode {
    private MethodExecutionMessage methodExecutionMessage;
    private MethodExecutionMessagesObjectNode parent;

    public MethodExecutionMessageNode(MethodExecutionMessagesObjectNode parent, MethodExecutionMessage methodExecutionMessage) {
        this.parent = parent;
        this.methodExecutionMessage = methodExecutionMessage;
    }

    public MethodExecutionMessage getExecutionMessage() {
        return methodExecutionMessage;
    }

    public DatabaseContentFile getVirtualFile() {
        return null;
    }

    public void dispose() {

    }

    public MessagesTreeModel getTreeModel() {
        return parent.getTreeModel();
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

    public TreeNode getParent() {
        return parent;
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
}