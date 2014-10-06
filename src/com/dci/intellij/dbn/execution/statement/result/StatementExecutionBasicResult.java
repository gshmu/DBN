package com.dci.intellij.dbn.execution.statement.result;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementViewerPopup;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class StatementExecutionBasicResult implements StatementExecutionResult{
    private String resultName;
    private StatementExecutionMessage executionMessage;
    private StatementExecutionStatus executionStatus;
    private int executionDuration;
    private int updateCount;
    private StatementViewerPopup statementViewerPopup;
    private CompilerResult compilerResult;
    private DBObjectRef<DBSchemaObject> affectedObjectRef;
    private StatementExecutionProcessor executionProcessor;

    public StatementExecutionBasicResult(
            StatementExecutionProcessor executionProcessor,
            String resultName,
            int updateCount) {
        this.resultName = resultName;
        this.executionProcessor = executionProcessor;
        this.updateCount = updateCount;
    }

    public String getResultName() {
        return resultName;
    }

    public Icon getResultIcon() {
        return executionProcessor == null || executionProcessor.isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    public StatementExecutionMessage getExecutionMessage() {
        return executionMessage;
    }

    public StatementExecutionInput getExecutionInput() {
        return executionProcessor == null ? null : executionProcessor.getExecutionInput();
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

    public StatementExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(StatementExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @Override
    public int getUpdateCount() {
        return updateCount;
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
    }

    public void updateExecutionMessage(MessageType messageType, String message) {
        executionMessage = new StatementExecutionMessage(this, message, "", messageType);
    }

    public void clearExecutionMessage() {
        if (executionMessage != null) {
            Disposer.dispose(executionMessage);
            executionMessage = null;
        }
    }

    public Project getProject() {
        return executionProcessor == null ? null : executionProcessor.getProject();
    }

    public ConnectionHandler getConnectionHandler() {
        return executionProcessor == null ? null : executionProcessor.getConnectionHandler();
    }

    public ExecutionResultForm getResultPanel() {
        return null;
    }

    @Override
    public CompilerResult getCompilerResult() {
        return compilerResult;
    }

    public void setCompilerResult(CompilerResult compilerResult) {
        this.compilerResult = compilerResult;
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
        executionProcessor = null;
        executionMessage = null;
    }
}
