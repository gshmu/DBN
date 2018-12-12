package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CloseExecutionResultAction extends MethodExecutionResultAction {
    public CloseExecutionResultAction() {
        super("Close", Icons.EXEC_RESULT_CLOSE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        MethodExecutionResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            ExecutionManager.getInstance(project).removeResultTab(executionResult);
        }
    }
}
