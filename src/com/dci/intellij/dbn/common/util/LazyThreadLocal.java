package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.NotNull;

public abstract class LazyThreadLocal<T> {
    private ThreadLocal<T> localValue = new ThreadLocal<T>();

    @NotNull
    public final T get() {
        T value = localValue.get();
        if (value == null) {
            value = load();
            localValue.set(value);
        }
        return value;
    }

    @NotNull
    protected abstract T load();
}
