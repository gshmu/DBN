package com.dci.intellij.dbn.execution.statement;

import java.util.EventListener;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.util.messages.Topic;

public interface StatementExecutionListener extends EventListener {
    Topic<StatementExecutionListener> TOPIC = Topic.create("Statement execution event", StatementExecutionListener.class);
    void dataModelChanged(DBSchema schema, DBObjectType objectType);
    void dataModelChanged(DBSchemaObject schemaObject);
    void transactionCreated(ConnectionHandler connectionHandler);
}
