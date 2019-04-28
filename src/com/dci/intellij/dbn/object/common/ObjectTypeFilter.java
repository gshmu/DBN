package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.type.DBObjectType;

public interface ObjectTypeFilter {
    boolean acceptsRootObject(DBObjectType objectType);

    boolean acceptsCurrentSchemaObject(DBObjectType objectType);

    boolean acceptsPublicSchemaObject(DBObjectType objectType);

    boolean acceptsAnySchemaObject(DBObjectType objectType);

    boolean acceptsObject(DBSchema schema, DBSchema currentSchema, DBObjectType objectType);
}
