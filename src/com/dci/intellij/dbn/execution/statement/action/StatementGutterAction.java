package com.dci.intellij.dbn.execution.statement.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class StatementGutterAction extends AnAction {
    final StatementExecutionProcessor executionProcessor;

    public StatementGutterAction(StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = executionProcessor;
    }

    public void actionPerformed(AnActionEvent e) {
        if (executionProcessor.canExecute()) {
            StatementExecutionManager executionManager = StatementExecutionManager.getInstance(executionProcessor.getProject());
            executionManager.fireExecution(executionProcessor);
        } else {
            executionProcessor.navigateToResult();
        }
    }

    @NotNull
    public Icon getIcon() {
        StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
        if (executionResult == null) {
            return Icons.STMT_EXECUTION_RUN;
        } else {
            if (executionResult.getExecutionStatus() == StatementExecutionResult.STATUS_SUCCESS){
                if (executionProcessor instanceof StatementExecutionCursorProcessor) {
                    return executionResult.getExecutionInput().isObsolete() ?
                            Icons.STMT_EXEC_RESULTSET_RERUN :
                            Icons.STMT_EXEC_RESULTSET;
                } else {
                    return Icons.EXEC_MESSAGES_INFO;
                }
            } else if (executionResult.getExecutionStatus() == StatementExecutionResult.STATUS_ERROR){
                return Icons.STMT_EXECUTION_ERROR_RERUN;
            }
        }

        return Icons.CHECK;
    }


    @Nullable
    public String getTooltipText() {
        if (executionProcessor.canExecute()) {
            return "<html>Execute <b>" + executionProcessor.getStatementName() + "</b></html>";
        } else {
            StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
            if (executionResult.getExecutionStatus() == StatementExecutionResult.STATUS_SUCCESS) {
                return "<html>Show execution result <br> <b>" + executionResult.getResultName() + "</b></html>";
            } else if (executionResult.getExecutionStatus() == StatementExecutionResult.STATUS_ERROR) {
                return "<html>Error executing statement <br> <font color='red'>" + executionResult.getExecutionMessage().getCauseMessage() + "</font></html>";
            }

        }
        return null;
    }

}
