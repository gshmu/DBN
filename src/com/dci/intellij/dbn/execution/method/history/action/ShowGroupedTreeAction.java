package com.dci.intellij.dbn.execution.method.history.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.history.ui.MethodExecutionHistoryTree;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;

public class ShowGroupedTreeAction extends ToggleAction {
    MethodExecutionHistoryTree tree;

    public ShowGroupedTreeAction(MethodExecutionHistoryTree tree) {
        super("Group by Program", "Show grouped by program", Icons.ACTION_GROUP);
        this.tree = tree;
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        return tree.isGrouped();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        getTemplatePresentation().setText(state ? "Ungroup" : "Group by Program");
        tree.showGrouped(state);
        Project project = ActionUtil.getProject(e);
        MethodExecutionManager.getInstance(project).getExecutionHistory().setGroupEntries(state);

    }
}
