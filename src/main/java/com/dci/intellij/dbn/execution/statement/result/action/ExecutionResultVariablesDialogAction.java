package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class ExecutionResultVariablesDialogAction extends AbstractExecutionResultAction {
    public ExecutionResultVariablesDialogAction() {
        super("Open Variables Dialog", Icons.EXEC_RESULT_OPEN_EXEC_DIALOG);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull StatementExecutionCursorResult executionResult) {
        StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
        StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(project);
        String statementName = executionResult.getExecutionProcessor().getStatementName();
        statementExecutionManager.promptExecutionDialog(
                executionProcessor,
                DBDebuggerType.NONE,
                () -> Progress.prompt(project, "Executing " + statementName, true,
                        progress -> {
                            try {
                                executionProcessor.execute();
                            } catch (SQLException ex) {
                                NotificationSupport.sendErrorNotification(
                                        project,
                                        NotificationGroup.EXECUTION,
                                        "Error executing statement. {0}", ex);
                            }
                        }));
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable StatementExecutionCursorResult executionResult) {
        boolean visible = false;
        if (Failsafe.check(executionResult)) {
            StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
            if (Failsafe.check(executionProcessor)) {
                StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
                visible = executionVariables != null && executionVariables.getVariables().size() > 0;
            }
        }
        presentation.setVisible(visible);
        presentation.setText("Open Variables Dialog");
    }
}
