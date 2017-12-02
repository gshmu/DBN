package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import static com.dci.intellij.dbn.execution.ExecutionStatus.EXECUTING;

public class StartMethodExecutionAction extends MethodExecutionResultAction {
    public StartMethodExecutionAction() {
        super("Execute Again", Icons.METHOD_EXECUTION_RERUN);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            MethodExecutionResult executionResult = getExecutionResult(e);
            if (executionResult != null) {
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                executionManager.execute(executionResult.getExecutionInput());
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Execute Again");
        presentation.setEnabled(
                executionResult != null &&
                        !executionResult.getDebuggerType().isDebug() &&
                        executionResult.getExecutionContext().isNot(EXECUTING));
    }
}