package com.dci.intellij.dbn.execution.statement.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class StatementGutterAction extends AnAction {
    final ExecutablePsiElement executablePsiElement;

    public StatementGutterAction(ExecutablePsiElement executablePsiElement) {
        this.executablePsiElement = executablePsiElement;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);

        StatementExecutionManager executionManager = getExecutionManager();
        if (executionManager != null) {
            if (executionProcessor == null) {
                executionProcessor = getExecutionProcessor(true);
                executionManager.fireExecution(executionProcessor);
            } else {
                StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
                if (executionResult != null && !executionResult.isDisposed()) {
                    executionProcessor.navigateToResult();
                } else {
                    executionManager.fireExecution(executionProcessor);
                }
            }
        }
    }

    @NotNull
    public Icon getIcon() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);
        if (executionProcessor != null) {
            StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
            if (executionResult == null) {
                return Icons.STMT_EXECUTION_RUN;
            } else {
                StatementExecutionStatus executionStatus = executionResult.getExecutionStatus();
                if (executionStatus == StatementExecutionStatus.SUCCESS){
                    if (executionProcessor instanceof StatementExecutionCursorProcessor) {
                        return executionResult.getExecutionInput().isObsolete() ?
                                Icons.STMT_EXEC_RESULTSET_RERUN :
                                Icons.STMT_EXEC_RESULTSET;
                    } else {
                        return Icons.EXEC_MESSAGES_INFO;
                    }
                } else if (executionStatus == StatementExecutionStatus.ERROR){
                    return Icons.STMT_EXECUTION_ERROR_RERUN;
                } else if (executionStatus == StatementExecutionStatus.WARNING){
                    return Icons.STMT_EXECUTION_WARNING;
                }
            }
        }


        return Icons.STMT_EXECUTION_RUN;
    }

    @Nullable
    private StatementExecutionManager getExecutionManager() {
        if (executablePsiElement.isValid()) {
            Project project = executablePsiElement.getProject();
            return StatementExecutionManager.getInstance(project);
        } else {
            return null;
        }
    }

    @Nullable
    private StatementExecutionProcessor getExecutionProcessor(boolean create) {
        StatementExecutionManager executionManager = getExecutionManager();
        if (executionManager != null) {
            return executionManager.getExecutionProcessor(executablePsiElement, create);
        }
        return null;
    }


    @Nullable
    public String getTooltipText() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);
        if (executionProcessor!= null && !executionProcessor.isDisposed()) {
            if (executionProcessor.canExecute()) {
                return "<html>Execute <b>" + executionProcessor.getStatementName() + "</b></html>";
            } else {
                StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
                StatementExecutionStatus executionStatus = executionResult.getExecutionStatus();
                if (executionStatus == StatementExecutionStatus.SUCCESS) {
                    return "<html>Show execution result <br> <b>" + executionResult.getResultName() + "</b></html>";
                } else if (executionStatus == StatementExecutionStatus.ERROR) {
                    return "<html>Error executing statement <br> <font color='red'>" + executionResult.getExecutionMessage().getCauseMessage() + "</font></html>";
                } else if (executionStatus == StatementExecutionStatus.WARNING) {
                    return "<html>Statement executed with warnings</html>";
                }
            }
        }
        return null;
    }

}
