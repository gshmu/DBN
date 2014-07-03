package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.TreePath;

public class CompilerMessagesNode extends BundleTreeNode {
    public CompilerMessagesNode(RootNode parent) {
        super(parent);
    }

    public MessagesTreeNode getChildTreeNode(VirtualFile virtualFile) {
        for (MessagesTreeNode messagesTreeNode : getChildren()) {
            if (messagesTreeNode.getVirtualFile().equals(virtualFile)) {
                return messagesTreeNode;
            }
        }
        return null;
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        CompilerMessagesObjectNode objectNode = (CompilerMessagesObjectNode)
                getChildTreeNode(compilerMessage.getDatabaseFile());
        if (objectNode == null) {
            objectNode = new CompilerMessagesObjectNode(this, compilerMessage.getDatabaseFile());
            addChild(objectNode);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return objectNode.addCompilerMessage(compilerMessage);
    }

    public TreePath getTreePath(CompilerMessage compilerMessage) {
        CompilerMessagesObjectNode objectNode = (CompilerMessagesObjectNode)
                getChildTreeNode(compilerMessage.getDatabaseFile());
        return objectNode.getTreePath(compilerMessage);
    }

    public VirtualFile getVirtualFile() {
        return null;
    }
}