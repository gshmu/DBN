package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

public class ExecutionResultVariablesDialogAction extends AbstractExecutionResultAction {
    public ExecutionResultVariablesDialogAction() {
        super("Open variables dialog", Icons.EXEC_RESULT_OPEN_EXEC_DIALOG);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            boolean continueExecution = executionResult.getExecutionProcessor().promptVariablesDialog();
            if (continueExecution) {
                new BackgroundTask(executionResult.getProject(), "Executing statement", false, true) {
                    protected void execute(@NotNull ProgressIndicator progressIndicator) {
                        initProgressIndicator(progressIndicator, true);
                        StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
                        executionProcessor.execute(progressIndicator);
                    }
                }.start();
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        boolean visible = false;
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
            if (executionProcessor != null) {
                StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
                visible = executionVariables != null && executionVariables.getVariables().size() > 0;
            }
        }
        e.getPresentation().setVisible(visible);
        e.getPresentation().setText("Open variables dialog");
    }
}
