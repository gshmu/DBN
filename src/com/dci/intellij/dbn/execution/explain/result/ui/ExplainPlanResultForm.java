package com.dci.intellij.dbn.execution.explain.result.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultFormBase;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ExplainPlanResultForm extends ExecutionResultFormBase<ExplainPlanResult> {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JScrollPane resultScrollPane;
    private ExplainPlanTreeTable explainPlanTreeTable;
    private JPanel resultPanel;

    public ExplainPlanResultForm(@NotNull ExplainPlanResult explainPlanResult) {
        super(explainPlanResult);
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", false, "DBNavigator.ActionGroup.ExplainPlanResult");
        actionToolbar.setTargetComponent(actionsPanel);

        actionsPanel.add(actionToolbar.getComponent());

        resultPanel.setBorder(IdeBorderFactory.createBorder());
        ExplainPlanTreeTableModel treeTableModel = new ExplainPlanTreeTableModel(explainPlanResult);
        explainPlanTreeTable = new ExplainPlanTreeTable(treeTableModel);

        resultScrollPane.setViewportView(explainPlanTreeTable);
        resultScrollPane.getViewport().setBackground(explainPlanTreeTable.getBackground());

        JPanel panel = new JPanel();
        panel.setBorder(UIUtil.getTableHeaderCellBorder());

        Disposer.register(this, explainPlanResult);
        Disposer.register(this, explainPlanTreeTable);
        ActionUtil.registerDataProvider(mainPanel, explainPlanResult);
    }

    public ExplainPlanTreeTable getExplainPlanTreeTable() {
        return explainPlanTreeTable;
    }

    public void show() {
        ExecutionResult executionResult = getExecutionResult();
        Project project = executionResult.getProject();
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.selectResultTab(executionResult);
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    public void collapseAllNodes() {
        TreeUtil.collapseAll(explainPlanTreeTable.getTree());    
    }

    public void expandAllNodes() {
        TreeUtil.expandAll(explainPlanTreeTable.getTree());
    }
}
