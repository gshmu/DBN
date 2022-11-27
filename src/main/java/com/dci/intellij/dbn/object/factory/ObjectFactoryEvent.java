package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.object.common.DBSchemaObject;
import lombok.Getter;

@Getter
public class ObjectFactoryEvent {
    public static final int EVENT_TYPE_CREATE = 0;
    public static final int EVENT_TYPE_DROP = 1;

    private final DBSchemaObject object;
    private final int eventType;

    public ObjectFactoryEvent(DBSchemaObject object, int eventType) {
        this.object = object;
        this.eventType = eventType;
    }
}
