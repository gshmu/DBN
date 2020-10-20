package com.dci.intellij.dbn.execution.statement.result;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionResultBase;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementExecutionResultForm;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StatementExecutionBasicResult extends ExecutionResultBase<StatementExecutionResultForm> implements StatementExecutionResult{
    private final String resultName;
    private StatementExecutionMessage executionMessage;
    private StatementExecutionStatus executionStatus;
    private int executionDuration;
    private final int updateCount;
    private CompilerResult compilerResult;
    private String loggingOutput;
    private boolean loggingActive;

    private StatementExecutionProcessor executionProcessor;
    private final ConnectionHandlerRef connectionHandler;
    private final SchemaId databaseSchema;

    public StatementExecutionBasicResult(
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull String resultName,
            int updateCount) {
        this.resultName = resultName;
        this.executionProcessor = executionProcessor;
        this.updateCount = updateCount;
        this.connectionHandler = Failsafe.nn(executionProcessor.getConnectionHandler()).getRef();
        this.databaseSchema = executionProcessor.getTargetSchema();
    }

    @Override
    public PsiFile createPreviewFile() {
        return getExecutionInput().createPreviewFile();
    }

    @Override
    @NotNull
    public String getName() {
        return resultName;
    }

    @Override
    public Icon getIcon() {
        return getExecutionProcessor().isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
    }

    @Override
    @NotNull
    public StatementExecutionProcessor getExecutionProcessor() {
        return Failsafe.nn(executionProcessor);
    }

    @Override
    public StatementExecutionMessage getExecutionMessage() {
        return executionMessage;
    }

    @Override
    @NotNull
    public StatementExecutionInput getExecutionInput() {
        return getExecutionProcessor().getExecutionInput();
    }

    @NotNull
    @Override
    public ExecutionContext getExecutionContext() {
        return getExecutionInput().getExecutionContext();
    }

    @Override
    public void navigateToEditor(NavigationInstructions instructions) {
          getExecutionProcessor().navigateToEditor(instructions);
    }

    @Override
    public int getExecutionDuration() {
        return executionDuration;
    }

    @Override
    public void calculateExecDuration() {
        this.executionDuration = (int) (System.currentTimeMillis() - getExecutionContext().getExecutionTimestamp());
    }

    @Override
    public StatementExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    @Override
    public void setExecutionStatus(StatementExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @Override
    public int getUpdateCount() {
        return updateCount;
    }

    @Override
    public void updateExecutionMessage(MessageType messageType, String message, String causeMessage) {
        executionMessage = new StatementExecutionMessage(this, message, causeMessage, messageType);
    }

    @Override
    public void updateExecutionMessage(MessageType messageType, String message) {
        executionMessage = new StatementExecutionMessage(this, message, "", messageType);
    }

    @Override
    public void clearExecutionMessage() {
        SafeDisposer.dispose(executionMessage);
        executionMessage = null;
    }

    @Override
    @NotNull
    public Project getProject() {
        return getExecutionProcessor().getProject();
    }

    @Override
    public ConnectionId getConnectionId() {
        return getExecutionInput().getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @Nullable
    public SchemaId getDatabaseSchema() {
        return databaseSchema;
    }

    @Nullable
    @Override
    public StatementExecutionResultForm createForm() {
        return null;
    }

    @Override
    public CompilerResult getCompilerResult() {
        return compilerResult;
    }

    @Override
    public boolean hasCompilerResult() {
        return compilerResult != null;
    }

    @Override
    public boolean isBulkExecution() {
        return getExecutionInput().isBulkExecution();
    }

    public void setCompilerResult(CompilerResult compilerResult) {
        this.compilerResult = compilerResult;
    }

    @Override
    public String getLoggingOutput() {
        return loggingOutput;
    }

    @Override
    public void setLoggingOutput(String loggingOutput) {
        this.loggingOutput = loggingOutput;
    }

    @Override
    public boolean isLoggingActive() {
        return loggingActive;
    }

    @Override
    public void setLoggingActive(boolean loggingActive) {
        this.loggingActive = loggingActive;
    }

    @Override
    public void disposeInner() {
        executionProcessor = null;
        super.disposeInner();
    }
}
