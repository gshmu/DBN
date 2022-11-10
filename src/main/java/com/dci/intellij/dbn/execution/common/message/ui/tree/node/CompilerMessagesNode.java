package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeBundleNode;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeNode;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeRootNode;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;

public class CompilerMessagesNode extends MessagesTreeBundleNode<MessagesTreeRootNode, CompilerMessagesObjectNode> {
    public CompilerMessagesNode(MessagesTreeRootNode parent) {
        super(parent);
    }

    @Nullable
    private MessagesTreeNode getChildTreeNode(VirtualFile virtualFile) {
        for (MessagesTreeNode messagesTreeNode : getChildren()) {
            VirtualFile nodeVirtualFile = messagesTreeNode.getVirtualFile();
            if (nodeVirtualFile != null && nodeVirtualFile.equals(virtualFile)) {
                return messagesTreeNode;
            }
        }
        return null;
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
        CompilerMessagesObjectNode objectNode = (CompilerMessagesObjectNode) getChildTreeNode(databaseFile);
        if (objectNode == null) {
            DBObjectRef<DBSchemaObject> objectRef = compilerMessage.getCompilerResult().getObjectRef();
            objectNode = new CompilerMessagesObjectNode(this, objectRef);
            addChild(objectNode);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return objectNode.addCompilerMessage(compilerMessage);
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
        CompilerMessagesObjectNode objectNode = (CompilerMessagesObjectNode) getChildTreeNode(databaseFile);
        if (objectNode != null) {
            return objectNode.getTreePath(compilerMessage);
        }
        return null;
    }
}