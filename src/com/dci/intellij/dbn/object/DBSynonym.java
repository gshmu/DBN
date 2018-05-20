package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.Nullable;

public interface DBSynonym extends DBSchemaObject {
    @Nullable
    DBObject getUnderlyingObject();
}