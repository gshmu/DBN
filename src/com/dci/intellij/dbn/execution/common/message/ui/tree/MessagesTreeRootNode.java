package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.common.message.ui.tree.node.CompilerMessagesNode;
import com.dci.intellij.dbn.execution.common.message.ui.tree.node.ExplainPlanMessagesNode;
import com.dci.intellij.dbn.execution.common.message.ui.tree.node.StatementExecutionMessagesNode;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class MessagesTreeRootNode extends MessagesTreeBundleNode<MessagesTreeNode, MessagesTreeBundleNode> {
    private MessagesTreeModel treeModel;

    MessagesTreeRootNode(MessagesTreeModel treeModel) {
        super(null);
        this.treeModel = treeModel;
    }

    TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        StatementExecutionMessagesNode execMessagesNode = null;
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof StatementExecutionMessagesNode) {
                execMessagesNode = (StatementExecutionMessagesNode) treeNode;
                break;
            }
        }
        if (execMessagesNode == null) {
            execMessagesNode = new StatementExecutionMessagesNode(this);
            addChild(execMessagesNode);
            treeModel.notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }

        return execMessagesNode.addExecutionMessage(executionMessage);
    }

    TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage) {
        ExplainPlanMessagesNode explainPlanMessagesNode = null;
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof ExplainPlanMessagesNode) {
                explainPlanMessagesNode = (ExplainPlanMessagesNode) treeNode;
                break;
            }
        }
        if (explainPlanMessagesNode == null) {
            explainPlanMessagesNode = new ExplainPlanMessagesNode(this);
            addChild(explainPlanMessagesNode);
            treeModel.notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }

        return explainPlanMessagesNode.addExplainPlanMessage(explainPlanMessage);
    }

    TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        CompilerMessagesNode compilerMessagesNode = getCompilerMessagesNode();
        if (compilerMessagesNode == null) {
            compilerMessagesNode = new CompilerMessagesNode(this);
            addChild(compilerMessagesNode);
            treeModel.notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return compilerMessagesNode.addCompilerMessage(compilerMessage);
    }

    @Nullable
    private CompilerMessagesNode getCompilerMessagesNode() {
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof CompilerMessagesNode) {
                return (CompilerMessagesNode) treeNode;
            }
        }
        return null;
    }

    @Nullable
    private StatementExecutionMessagesNode getStatementExecutionMessagesNode() {
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof StatementExecutionMessagesNode) {
                return (StatementExecutionMessagesNode) treeNode;
            }
        }
        return null;
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        CompilerMessagesNode compilerMessagesNode = getCompilerMessagesNode();
        if (compilerMessagesNode != null) {
            return compilerMessagesNode.getTreePath(compilerMessage);
        }
        return null;
    }

    @Nullable
    public TreePath getTreePath(StatementExecutionMessage executionMessage) {
        StatementExecutionMessagesNode executionMessagesNode = getStatementExecutionMessagesNode();
        if (executionMessagesNode != null) {
            return executionMessagesNode.getTreePath(executionMessage);
        }
        return null;
    }


    @Override
    public MessagesTreeModel getTreeModel() {
        return Failsafe.nn(treeModel);
    }
}
