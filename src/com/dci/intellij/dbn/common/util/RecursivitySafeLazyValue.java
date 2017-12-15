package com.dci.intellij.dbn.common.util;

public abstract class RecursivitySafeLazyValue<T> implements LazyValue<T> {
    private T value;
    private boolean loading; // recursivity check

    public RecursivitySafeLazyValue() {
    }

    public RecursivitySafeLazyValue(T defaultValue) {
        this.value = defaultValue;
    }

    @Override
    public final T get(){
        if (value == null && !loading) {
            synchronized (this) {
                if (value == null && !loading) {
                    try {
                        loading = true;
                        value = load();
                    } finally {
                        loading = false;
                    }

                }
            }
        }
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public void reset() {
        set(null);
    }

    @Override
    public boolean isLoaded() {
        return value != null;
    }

    protected abstract T load();
}
