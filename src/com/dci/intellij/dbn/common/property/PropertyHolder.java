package com.dci.intellij.dbn.common.property;

public interface PropertyHolder<T extends Property> {
    boolean set(T status, boolean value);

    boolean is(T status);

    default boolean isNot(T status) {
        return !is(status);
    };

    static <T extends Property.LongBase> PropertyHolder<T> longBase(Class<T> type) {
        return new PropertyHolderBase.LongStore<T>() {
            @Override
            protected T[] properties() {
                return type.getEnumConstants();
            }
        };
    }

    static <T extends Property.IntBase> PropertyHolder<T> integerBase(Class<T> type) {
        return new PropertyHolderBase.IntStore<T>() {
            @Override
            protected T[] properties() {
                return type.getEnumConstants();
            }
        };
    }

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
