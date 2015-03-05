package com.dci.intellij.dbn.execution.method;

public interface ArgumentValueStore<T> {
    T getValue();
    void setValue(T value);
}
