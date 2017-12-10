package com.dci.intellij.dbn.common.util;

public abstract class SimpleLazyValue<T> implements LazyValue<T> {
    private T value;
    private boolean loading; // recursivity check

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
