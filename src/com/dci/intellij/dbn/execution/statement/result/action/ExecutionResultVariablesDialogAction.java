package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class ExecutionResultVariablesDialogAction extends AbstractExecutionResultAction {
    public ExecutionResultVariablesDialogAction() {
        super("Open variables dialog", Icons.EXEC_RESULT_OPEN_EXEC_DIALOG);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
            Project project = executionResult.getProject();
            StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(project);
            statementExecutionManager.promptExecutionDialog(
                    executionProcessor,
                    DBDebuggerType.NONE,
                    () -> Progress.prompt(project, "Executing " + executionResult.getExecutionProcessor().getStatementName(), true,
                            (progress) -> {
                                try {
                                    executionProcessor.execute();
                                } catch (SQLException ex) {
                                    NotificationUtil.sendErrorNotification(project, "Error executing statement", ex.getMessage());
                                }
                            }));
        }
    }

    @Override
    public void update(AnActionEvent e) {
        boolean visible = false;
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
            StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
            visible = executionVariables != null && executionVariables.getVariables().size() > 0;
        }
        e.getPresentation().setVisible(visible);
        e.getPresentation().setText("Open Variables Dialog");
    }
}
