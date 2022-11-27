package com.dci.intellij.dbn.database.interfaces.queue;

public enum InterfaceTaskStatus {
    NEW,
    QUEUED,
    DEQUEUED,
    SCHEDULED,
    STARTED,
    FINISHED,
    RELEASED
}
