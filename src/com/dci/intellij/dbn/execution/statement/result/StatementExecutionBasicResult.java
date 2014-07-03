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
import com.intellij.openapi.util.Disposer;
import sun.java2d.DisposerRecord;

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

        Disposer.register(this, executionInput);
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
        Disposer.register(this, statementViewerPopup);
    }

    public void updateExecutionMessage(MessageType messageType, String message, String causeMessage) {
        executionMessage = new StatementExecutionMessage(this, message, causeMessage, messageType);
        Disposer.register(this, executionMessage);
    }

    public void updateExecutionMessage(MessageType messageType, String message) {
        executionMessage = new StatementExecutionMessage(this, message, "", messageType);
        Disposer.register(this, executionMessage);
    }

    public void clearExecutionMessage() {
        if (executionMessage != null) {
            Disposer.dispose(executionMessage);
            executionMessage = null;
        }
    }

    public Project getProject() {
        StatementExecutionBasicProcessor executionProcessor = getExecutionProcessor();
        return executionProcessor == null ? null : executionProcessor.getProject();
    }

    public ConnectionHandler getConnectionHandler() {
        return executionInput.getConnectionHandler();
    }

    public ExecutionResultForm getResultPanel() {
        return null;
    }


    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;
        statementViewerPopup = null;
        executionInput = null;
        executionMessage = null;
    }
}
