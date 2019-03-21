package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class StatementExecutionMessage extends ConsoleMessage {
    private String causeMessage;
    private StatementExecutionResult executionResult;

    public StatementExecutionMessage(StatementExecutionResult executionResult, String message, String causeMessage, MessageType messageType) {
        super(messageType, message);
        this.executionResult = executionResult;
        this.causeMessage = causeMessage;
        Disposer.register(this, executionResult);
    }

    public StatementExecutionResult getExecutionResult() {
        return executionResult;
    }

    public VirtualFile getVirtualFile() {
        return executionResult.getExecutionProcessor().getVirtualFile();
    }

    public boolean isOrphan() {
        StatementExecutionProcessor executionProcessor = executionResult.getExecutionProcessor();
        return executionProcessor.isDirty() ||
                executionProcessor.getExecutionResult() != executionResult; // overwritten result
    }

    @Override
    public boolean isNew() {
        return super.isNew()/* && !isOrphan()*/;
    }

    public String getCauseMessage() {
        return causeMessage;
    }

    public void createStatementViewer() {
        
    }
}
