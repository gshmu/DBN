package com.dci.intellij.dbn.execution.statement.result;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;

public class StatementExecutionBasicResult implements StatementExecutionResult{
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
    private DBObjectRef<DBSchema> currentSchemaRef;

    public StatementExecutionBasicResult(
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull String resultName,
            int updateCount) {
        this.resultName = resultName;
        this.executionProcessor = executionProcessor;
        this.updateCount = updateCount;
        this.connectionHandlerRef = FailsafeUtil.get(executionProcessor.getConnectionHandler()).getRef();
        this.currentSchemaRef = DBObjectRef.from(executionProcessor.getCurrentSchema());
    }

    @Override
    public PsiFile createPreviewFile() {
        return getExecutionInput().createPreviewFile();
    }

    @NotNull
    public String getName() {
        return resultName;
    }

    public Icon getIcon() {
        return getExecutionProcessor().isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
    }

    @NotNull
    public StatementExecutionProcessor getExecutionProcessor() {
        return FailsafeUtil.get(executionProcessor);
    }

    public StatementExecutionMessage getExecutionMessage() {
        return executionMessage;
    }

    @NotNull
    public StatementExecutionInput getExecutionInput() {
        return getExecutionProcessor().getExecutionInput();
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

    @NotNull
    public Project getProject() {
        return getExecutionProcessor().getProject();
    }

    @Override
    public String getConnectionId() {
        return getExecutionInput().getConnectionId();
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Nullable
    public DBSchema getCurrentSchema() {
        return DBObjectRef.get(currentSchemaRef);
    }

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
        executionProcessor = null;
        executionMessage = null;
    }

    @Nullable
    @Override
    public DataProvider getDataProvider() {
        return null;
    }
}
