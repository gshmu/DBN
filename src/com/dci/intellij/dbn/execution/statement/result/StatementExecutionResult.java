package com.dci.intellij.dbn.execution.statement.result;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementExecutionResultForm;
import org.jetbrains.annotations.NotNull;

public interface StatementExecutionResult extends ExecutionResult<StatementExecutionResultForm> {
    @NotNull StatementExecutionProcessor getExecutionProcessor();
    StatementExecutionMessage getExecutionMessage();
    StatementExecutionInput getExecutionInput();
    @NotNull ExecutionContext getExecutionContext();

    StatementExecutionStatus getExecutionStatus();

    void setExecutionStatus(StatementExecutionStatus executionStatus);
    void updateExecutionMessage(MessageType messageType, String message, String causeMessage);
    void updateExecutionMessage(MessageType messageType, String message);
    void clearExecutionMessage();
    void calculateExecDuration();
    int getExecutionDuration();



    void navigateToEditor(NavigationInstructions instructions);

    int getUpdateCount();

    CompilerResult getCompilerResult();
    boolean hasCompilerResult();
    boolean isBulkExecution();

    String getLoggingOutput();
    void setLoggingOutput(String loggerOutput);
    boolean isLoggingActive();
    void setLoggingActive(boolean databaseLogActive);
}
