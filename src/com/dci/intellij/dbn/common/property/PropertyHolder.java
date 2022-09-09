package com.dci.intellij.dbn.common.property;

public interface PropertyHolder<T extends Property> {
    boolean set(T status, boolean value);

    boolean is(T status);

    default boolean isNot(T status) {
        return !is(status);
    };

    default void sync(T property, Runnable runnable) {
        if(isNot(property)) {
            synchronized (this) {
                if (isNot(property)) {
                    try {
                        set(property, true);
                        runnable.run();
                    } finally {
                        set(property, false);
                    }
                }
            }
        }
    }
}
