package com.dci.intellij.dbn.common.util;

public interface LazyValue<T> {
    T get();
    void set(T value);
    void reset();
    boolean isLoaded();
}
