package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface ObjectFactoryListener {
    void objectCreated(DBSchemaObject object);
    void objectDropped(DBSchemaObject object);
}
