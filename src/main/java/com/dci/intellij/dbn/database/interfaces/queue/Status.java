package com.dci.intellij.dbn.database.interfaces.queue;

public interface Status {
    int ordinal();

    boolean isRightAfter(Status status);

    boolean isAfter(Status status);
}
