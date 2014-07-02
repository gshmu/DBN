package com.dci.intellij.dbn.execution.statement.result;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionBasicProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementViewerPopup;
import com.intellij.openapi.project.Project;

import javax.swing.Icon;

public class StatementExecutionBasicResult implements StatementExecutionResult{
    private String resultName;
    private StatementExecutionMessage executionMessage;
    private StatementExecutionInput executionInput;
    private int executionDuration;
    private int executionStatus;
    private StatementViewerPopup statementViewerPopup;

    public StatementExecutionBasicResult(String resultName,
            StatementExecutionInput executionInput) {
        this.resultName = resultName;
        this.executionInput = executionInput;
    }

    public String getResultName() {
        return resultName;
    }

    public Icon getResultIcon() {
        return isOrphan() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
    }

    public StatementExecutionBasicProcessor getExecutionProcessor() {
        return executionInput == null ? null : (StatementExecutionBasicProcessor) executionInput.getExecutionProcessor();
    }

    public StatementExecutionMessage getExecutionMessage() {
        return executionMessage;
    }

    public void setExecutionInput(StatementExecutionInput executionInput) {
        this.executionInput = executionInput;
    }

    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    public boolean isOrphan() {
        return getExecutionProcessor().isOrphan();
    }

    public void navigateToEditor(boolean requestFocus) {
          getExecutionProcessor().navigateToEditor(requestFocus);
    }

    public int getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(int executionDuration) {
        this.executionDuration = executionDuration;
    }

    public int getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(int executionStatus) {
        this.executionStatus = executionStatus;
    }

    public StatementViewerPopup getStatementViewerPopup() {
        return statementViewerPopup;
    }

    public void setStatementViewerPopup(StatementViewerPopup statementViewerPopup) {
        this.statementViewerPopup = statementViewerPopup;
    }

    public void updateExecutionMessage(MessageType messageType, String message, String causeMessage) {
        executionMessage = new StatementExecutionMessage(this, message, causeMessage, messageType);
    }

    public void updateExecutionMessage(MessageType messageType, String message) {
        executionMessage = new StatementExecutionMessage(this, message, "", messageType);
    }

    public void clearExecutionMessage() {
        executionMessage = null;
    }

    public Project getProject() {
        StatementExecutionBasicProcessor executionProcessor = getExecutionProcessor();
        return executionProcessor == null ? null : executionProcessor.getProject();
    }

    public ConnectionHandler getConnectionHandler() {
        return executionInput.getConnectionHandler();
    }

    public void dispose() {
        executionInput.dispose();
        if (statementViewerPopup != null) {
            statementViewerPopup.dispose();
            statementViewerPopup = null;
        }
        executionInput = null;
        executionMessage = null;
    }

    public ExecutionResultForm getResultPanel() {
        return null;
    }
}
