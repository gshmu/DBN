package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatementExecutionMessage extends ConsoleMessage {
    private final String causeMessage;
    private StatementExecutionResult executionResult;
    private final ConnectionId connectionId;

    public StatementExecutionMessage(StatementExecutionResult executionResult, String message, String causeMessage, MessageType messageType) {
        super(messageType, message);
        this.executionResult = executionResult;
        this.causeMessage = causeMessage;
        this.connectionId = executionResult.getConnectionId();
    }

    public VirtualFile getVirtualFile() {
        VirtualFile virtualFile = executionResult.getExecutionProcessor().getVirtualFile();
        return Failsafe.nn(virtualFile);
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

    public void createStatementViewer() {
        
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(executionResult);
        super.disposeInner();
    }
}
