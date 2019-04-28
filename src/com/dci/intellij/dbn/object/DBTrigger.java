package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.type.DBTriggerEvent;
import com.dci.intellij.dbn.object.type.DBTriggerType;

public interface DBTrigger extends DBSchemaObject {
    boolean isForEachRow();
    DBTriggerType getTriggerType();
    DBTriggerEvent[] getTriggerEvents();

}