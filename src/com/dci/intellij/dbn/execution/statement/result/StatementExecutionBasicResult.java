package com.dci.intellij.dbn.execution.statement.result;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StatementExecutionBasicResult extends DisposableBase implements StatementExecutionResult{
    private String resultName;
    private StatementExecutionMessage executionMessage;
    private StatementExecutionStatus executionStatus;
    private int executionDuration;
    private int updateCount;
    private CompilerResult compilerResult;
    private StatementExecutionProcessor executionProcessor;
    private String loggingOutput;
    private boolean loggingActive;

    private ConnectionHandlerRef connectionHandlerRef;
    private SchemaId databaseSchema;

    public StatementExecutionBasicResult(
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull String resultName,
            int updateCount) {
        this.resultName = resultName;
        this.executionProcessor = executionProcessor;
        this.updateCount = updateCount;
        this.connectionHandlerRef = Failsafe.get(executionProcessor.getConnectionHandler()).getRef();
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
        return Failsafe.get(executionProcessor);
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
    public void navigateToEditor(NavigationInstruction instruction) {
          getExecutionProcessor().navigateToEditor(instruction);
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
        if (executionMessage != null) {
            Disposer.dispose(executionMessage);
            executionMessage = null;
        }
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
        return connectionHandlerRef.getnn();
    }

    @Nullable
    public SchemaId getDatabaseSchema() {
        return databaseSchema;
    }

    @Override
    public ExecutionResultForm getForm(boolean create) {
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

    @Nullable
    @Override
    public DataProvider getDataProvider() {
        return null;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    @Override
    public void dispose() {
        super.dispose();
        executionProcessor = null;
        executionMessage = null;
    }
}
