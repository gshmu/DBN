package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.method.MethodExecutionMessage;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class MethodExecutionMessageNode extends DisposableBase implements MessageTreeNode {
    private MethodExecutionMessage methodExecutionMessage;
    private MethodExecutionMessagesObjectNode parent;

    public MethodExecutionMessageNode(MethodExecutionMessagesObjectNode parent, MethodExecutionMessage methodExecutionMessage) {
        this.parent = parent;
        this.methodExecutionMessage = methodExecutionMessage;

        Disposer.register(this, methodExecutionMessage);
    }

    public MethodExecutionMessage getExecutionMessage() {
        return FailsafeUtil.get(methodExecutionMessage);
    }

    @Override
    public DBContentVirtualFile getVirtualFile() {
        return null;
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
    public MethodExecutionMessagesObjectNode getParent() {
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

    @NotNull
    @Override
    public ConsoleMessage getMessage() {
        return getExecutionMessage();
    }

    @Override
    public void dispose() {
        super.dispose();
        methodExecutionMessage = null;
        parent = null;
    }
}