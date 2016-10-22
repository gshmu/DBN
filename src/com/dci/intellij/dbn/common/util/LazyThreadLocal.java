package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.NotNull;

public abstract class LazyThreadLocal<T> extends ThreadLocal<T>{

    @NotNull
    public final T get() {
        T value = super.get();
        if (value == null) {
            value = load();
            set(value);
        }
        return value;
    }

    @NotNull
    protected abstract T load();
}
