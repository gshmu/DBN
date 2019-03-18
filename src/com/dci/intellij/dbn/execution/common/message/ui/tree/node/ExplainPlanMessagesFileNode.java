package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeBundleNode;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;

public class ExplainPlanMessagesFileNode extends MessagesTreeBundleNode<ExplainPlanMessagesNode, ExplainPlanMessageNode> {
    private VirtualFile virtualFile;

    ExplainPlanMessagesFileNode(ExplainPlanMessagesNode parent, VirtualFile virtualFile) {
        super(parent);
        this.virtualFile = virtualFile;
    }

    @NotNull
    @Override
    public VirtualFile getVirtualFile() {
        return Failsafe.get(virtualFile);
    }

    TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage) {
        ExplainPlanMessageNode explainPlanMessageNode = new ExplainPlanMessageNode(this, explainPlanMessage);
        addChild(explainPlanMessageNode);
        getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        return TreeUtil.createTreePath(explainPlanMessageNode);
    }

    @Nullable
    public TreePath getTreePath(ExplainPlanMessage explainPlanMessage) {
        for (ExplainPlanMessageNode messageNode : getChildren()) {
            if (messageNode.getMessage() == explainPlanMessage) {
                return TreeUtil.createTreePath(messageNode);
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        virtualFile = null;
    }
}
