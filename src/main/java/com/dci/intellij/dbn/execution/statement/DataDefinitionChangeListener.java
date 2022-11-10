package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

public interface DataDefinitionChangeListener extends EventListener {
    Topic<DataDefinitionChangeListener> TOPIC = Topic.create("Data Model event", DataDefinitionChangeListener.class);
    void dataDefinitionChanged(@NotNull DBSchemaObject schemaObject);
    void dataDefinitionChanged(DBSchema schema, DBObjectType objectType);
}
