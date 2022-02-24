package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeLeafNode;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;

public class StatementExecutionMessageNode extends MessagesTreeLeafNode<StatementExecutionMessagesFileNode, StatementExecutionMessage> {

    StatementExecutionMessageNode(StatementExecutionMessagesFileNode parent, StatementExecutionMessage executionMessage) {
        super(parent, executionMessage);
    }

    @Override
    public String toString() {
        StatementExecutionMessage executionMessage = getMessage();
        return
            executionMessage.getText() + " " +
            executionMessage.getCauseMessage() + " - Connection: " +
            executionMessage.getExecutionResult().getConnection().getName() + ": " +
            executionMessage.getExecutionResult().getExecutionDuration() + "ms";
    }
}
