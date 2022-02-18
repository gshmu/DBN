package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeBundleNode;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.List;

public class CompilerMessagesObjectNode extends MessagesTreeBundleNode<CompilerMessagesNode, CompilerMessageNode> {
    private final DBObjectRef<DBSchemaObject> object;

    CompilerMessagesObjectNode(CompilerMessagesNode parent, DBObjectRef<DBSchemaObject> object) {
        super(parent);
        this.object = object;
    }

    @Override
    @Nullable
    public DBEditableObjectVirtualFile getVirtualFile() {
        DBSchemaObject schemaObject = getObject();
        if (schemaObject != null) {
            return schemaObject.getEditableVirtualFile();
        }
        return null;
    }

    @Nullable
    @Override
    public ConnectionId getConnectionId() {
        return object.getConnectionId();
    }

    @Nullable
    public DBSchemaObject getObject() {
        return DBObjectRef.get(object);
    }

    public DBObjectRef<DBSchemaObject> getObjectRef() {
        return object;
    }

    TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        List<CompilerMessageNode> children = getChildren();
        if (children.size() > 0) {
            CompilerMessageNode firstChild = children.get(0);
            if (firstChild.getMessage().getCompilerResult() != compilerMessage.getCompilerResult()) {
                clearChildren();
            }
        }
        CompilerMessageNode messageNode = new CompilerMessageNode(this, compilerMessage);
        addChild(messageNode);

        getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        return TreeUtil.createTreePath(messageNode);
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        for (CompilerMessageNode messageNode : getChildren()) {
            if (messageNode.getMessage() == compilerMessage) {
                return TreeUtil.createTreePath(messageNode);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
