package com.dci.intellij.dbn.execution.statement.result;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementViewerPopup;

public interface StatementExecutionResult extends ExecutionResult {
    StatementExecutionProcessor getExecutionProcessor();
    StatementExecutionMessage getExecutionMessage();
    StatementExecutionInput getExecutionInput();

    StatementExecutionStatus getExecutionStatus();

    void setExecutionStatus(StatementExecutionStatus executionStatus);
    void updateExecutionMessage(MessageType messageType, String message, String causeMessage);
    void updateExecutionMessage(MessageType messageType, String message);
    void clearExecutionMessage();

    void navigateToEditor(boolean requestFocus);

    int getUpdateCount();

    StatementViewerPopup getStatementViewerPopup();
    void setStatementViewerPopup(StatementViewerPopup statementViewerPopup);

    CompilerResult getCompilerResult();
    boolean hasCompilerResult();
    boolean isBulkExecution();
}
