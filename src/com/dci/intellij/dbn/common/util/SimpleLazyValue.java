package com.dci.intellij.dbn.common.util;

public abstract class SimpleLazyValue<T> implements LazyValue<T> {
    private T value;

    @Override
    public final T get(){
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = load();
                }
            }
        }
        return value;
    }

    @Override
    public boolean isLoaded() {
        return value != null;
    }

    protected abstract T load();
}
