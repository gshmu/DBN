package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class ExecutionResultCloseAction extends AbstractExecutionResultAction {
    public ExecutionResultCloseAction() {
        super("Close", Icons.EXEC_RESULT_CLOSE);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null && executionResult.getProject() != null) {
            Project project = executionResult.getProject();
            ExecutionManager executionManager = ExecutionManager.getInstance(project);
            executionManager.removeResultTab(executionResult);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Close");
    }
}
