package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class RootNode extends BundleTreeNode {
    private MessagesTreeModel messagesTreeModel;

    public RootNode(MessagesTreeModel messagesTreeModel) {
        super(null);
        this.messagesTreeModel = messagesTreeModel;
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        StatementExecutionMessagesNode execMessagesNode = null;
        for (TreeNode treeNode : children) {
            if (treeNode instanceof StatementExecutionMessagesNode) {
                execMessagesNode = (StatementExecutionMessagesNode) treeNode;
                break;
            }
        }
        if (execMessagesNode == null) {
            execMessagesNode = new StatementExecutionMessagesNode(this);
            children.add(execMessagesNode);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }

        return execMessagesNode.addExecutionMessage(executionMessage);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        CompilerMessagesNode compilerMessagesNode = getCompilerMessagesNode();
        if (compilerMessagesNode == null) {
            compilerMessagesNode = new CompilerMessagesNode(this);
            children.add(compilerMessagesNode);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return compilerMessagesNode.addCompilerMessage(compilerMessage);
    }

    public CompilerMessagesNode getCompilerMessagesNode() {
        for (TreeNode treeNode : children) {
            if (treeNode instanceof CompilerMessagesNode) {
                return (CompilerMessagesNode) treeNode;
            }
        }
        return null;
    }

    public TreePath getTreePath(CompilerMessage compilerMessage) {
        CompilerMessagesNode compilerMessagesNode = getCompilerMessagesNode();
        return compilerMessagesNode.getTreePath(compilerMessage);
    }


    public MessagesTreeModel getTreeModel() {
        return messagesTreeModel;
    }

    public VirtualFile getVirtualFile() {
        return null;
    }
}
