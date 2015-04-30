package com.dci.intellij.dbn.common.ui;

public interface ValueSelectorListener<T> {
    void selectionChanged(T oldValue, T newValue);
}
