package com.dci.intellij.dbn.common.util;

public interface LazyValue<T> {
    T get();
    boolean isLoaded();
}
