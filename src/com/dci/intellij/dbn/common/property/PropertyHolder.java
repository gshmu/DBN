package com.dci.intellij.dbn.common.property;

public interface PropertyHolder<T extends Property> {
    void set(T status, boolean value);

    boolean is(T status);
}
