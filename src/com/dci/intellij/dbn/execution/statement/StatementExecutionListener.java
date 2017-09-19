package com.dci.intellij.dbn.execution.statement;

import java.util.EventListener;

import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.util.messages.Topic;

public interface StatementExecutionListener extends EventListener {
    Topic<StatementExecutionListener> TOPIC = Topic.create("Statement execution event", StatementExecutionListener.class);
    void statementExecuted(StatementExecutionProcessor processor);
}
