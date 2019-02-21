package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import java.util.List;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class MethodExecutionHistoryTree extends DBNTree implements Disposable {
    private MethodExecutionHistoryDialog dialog;
    private MethodExecutionHistory executionHistory;
    private boolean grouped;
    private boolean debug;

    MethodExecutionHistoryTree(MethodExecutionHistoryDialog dialog, MethodExecutionHistory executionHistory, boolean grouped, boolean debug) {
        super(grouped ?
                new MethodExecutionHistoryGroupedTreeModel(executionHistory.getExecutionInputs(), debug) :
                new MethodExecutionHistorySimpleTreeModel(executionHistory.getExecutionInputs(), debug));
        this.executionHistory = executionHistory;
        this.dialog = dialog;
        this.grouped = grouped;
        this.debug = debug;
        setCellRenderer(new TreeCellRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeUtil.expand(this, 4);

        addTreeSelectionListener(treeSelectionListener);
        getModel().addTreeModelListener(treeModelListener);
    }

    public Project getProject() {
        return dialog.getProject();
    }

    void showGrouped(boolean grouped) {
        List<MethodExecutionInput> executionInputs = executionHistory.getExecutionInputs();
        MethodExecutionHistoryTreeModel model = grouped ?
                new MethodExecutionHistoryGroupedTreeModel(executionInputs, debug) :
                new MethodExecutionHistorySimpleTreeModel(executionInputs, debug);
        model.addTreeModelListener(treeModelListener);
        setModel(model);
        TreeUtil.expand(this, 4);
        this.grouped = grouped;
    }

    void setSelectedInput(MethodExecutionInput executionInput) {
        if (executionInput != null) {
            MethodExecutionHistoryTreeModel model = (MethodExecutionHistoryTreeModel) getModel();
            getSelectionModel().setSelectionPath(model.getTreePath(executionInput));
        }
    }

    boolean isGrouped() {
        return grouped;
    }

    @Nullable
    MethodExecutionInput getSelectedExecutionInput() {
        Object selection = getLastSelectedPathComponent();
        if (selection instanceof MethodExecutionHistoryTreeModel.MethodTreeNode) {
            MethodExecutionHistoryTreeModel.MethodTreeNode methodNode = (MethodExecutionHistoryTreeModel.MethodTreeNode) selection;
            return methodNode.getExecutionInput();
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        executionHistory = null;
        dialog = null;
    }

    private class TreeCellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Failsafe.lenient(() -> {
                MethodExecutionHistoryTreeNode node = (MethodExecutionHistoryTreeNode) value;
                setIcon(node.getIcon());
                append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                if (node instanceof MethodExecutionHistoryTreeModel.MethodTreeNode) {
                    MethodExecutionHistoryTreeModel.MethodTreeNode methodTreeNode = (MethodExecutionHistoryTreeModel.MethodTreeNode) node;
                    int overload = methodTreeNode.getOverload();
                    if (overload > 0) {
                        append(" #" + overload, SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                }
            });
        }
    }

    void removeSelectedEntries() {
        MethodExecutionHistoryTreeNode treeNode = (MethodExecutionHistoryTreeNode)
                getSelectionPath().getLastPathComponent();
        MethodExecutionHistoryTreeNode parentTreeNode = (MethodExecutionHistoryTreeNode) treeNode.getParent();
        while (parentTreeNode != null &&
                parentTreeNode.getChildCount() == 1 && 
                !parentTreeNode.isRoot()) {
            getSelectionModel().setSelectionPath(TreeUtil.getPathFromRoot(parentTreeNode));
            parentTreeNode = (MethodExecutionHistoryTreeNode) parentTreeNode.getParent();
        }
        TreeUtil.removeSelected(this);
    }

    /**********************************************************
     *                         Listeners                      *
     **********************************************************/
    private TreeSelectionListener treeSelectionListener = new TreeSelectionListener(){
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            MethodExecutionInput executionInput = getSelectedExecutionInput();
            if (executionInput != null) {
                ConnectionAction.invoke(
                        instructions("Loading method details"),
                        "loading the execution history", executionInput,
                        action -> {
                            DBMethod method = executionInput.getMethod();
                            if (method != null) {
                                method.getArguments();
                            }

                            SimpleLaterInvocator.invoke(() -> {
                                Failsafe.ensure(dialog);
                                dialog.showMethodExecutionPanel(executionInput);
                                dialog.setSelectedExecutionInput(executionInput);
                                dialog.updateMainButtons(executionInput);
                                if (method != null) {
                                    executionHistory.setSelection(executionInput.getMethodRef());
                                }
                            });
                        });
            }
        }
    };

    private TreeModelListener treeModelListener = new TreeModelHandler() {
        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            dialog.setSaveButtonEnabled(true);
        }
    };
}
