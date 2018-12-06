package com.dci.intellij.dbn.common.property;

public interface PropertyHolder<T extends Property> {
    boolean set(T status, boolean value);

    boolean is(T status);

    default boolean isNot(T status) {
        return !is(status);
    };
}
