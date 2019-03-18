package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeBundleNode;
import com.dci.intellij.dbn.execution.method.MethodExecutionMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;

public class MethodExecutionMessagesObjectNode extends MessagesTreeBundleNode<MethodExecutionMessagesNode, MethodExecutionMessageNode> {
    private DBEditableObjectVirtualFile databaseFile;

    MethodExecutionMessagesObjectNode(@NotNull MethodExecutionMessagesNode parent, @NotNull DBEditableObjectVirtualFile databaseFile) {
        super(parent);
        this.databaseFile = databaseFile;
    }

    @NotNull
    @Override
    public DBEditableObjectVirtualFile getVirtualFile() {
        return Failsafe.get(databaseFile);
    }

    public DBSchemaObject getObject() {
        return databaseFile.getObject();
    }

    TreePath addCompilerMessage(MethodExecutionMessage executionMessage) {
        clearChildren();
        MethodExecutionMessageNode messageNode = new MethodExecutionMessageNode(this, executionMessage);
        addChild(messageNode);

        TreePath treePath = TreeUtil.createTreePath(this);
        getTreeModel().notifyTreeModelListeners(treePath, TreeEventType.STRUCTURE_CHANGED);
        return treePath;
    }

    @Nullable
    public TreePath getTreePath(MethodExecutionMessage executionMessage) {
        for (MethodExecutionMessageNode messageNode : getChildren()) {
            if (messageNode.getMessage() == executionMessage) {
                return TreeUtil.createTreePath(messageNode);
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        databaseFile = null;
    }
}
