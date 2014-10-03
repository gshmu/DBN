package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreePath;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.vfs.VirtualFile;

public class CompilerMessagesNode extends BundleTreeNode {
    public CompilerMessagesNode(RootNode parent) {
        super(parent);
    }

    public MessagesTreeNode getChildTreeNode(VirtualFile virtualFile) {
        for (MessagesTreeNode messagesTreeNode : getChildren()) {
            VirtualFile nodeVirtualFile = messagesTreeNode.getVirtualFile();
            if (nodeVirtualFile != null && nodeVirtualFile.equals(virtualFile)) {
                return messagesTreeNode;
            }
        }
        return null;
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        CompilerMessagesObjectNode objectNode = (CompilerMessagesObjectNode)
                getChildTreeNode(compilerMessage.getDatabaseFile());
        if (objectNode == null) {
            DBObjectRef<DBSchemaObject> objectRef = compilerMessage.getCompilerResult().getObjectRef();
            objectNode = new CompilerMessagesObjectNode(this, objectRef);
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