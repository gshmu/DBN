package com.dci.intellij.dbn.common.util;

public abstract class LazyValue<T>{

    private T value;
    private boolean loaded = false;
    public final synchronized T get(){
        if (!loaded) {
            value = load();
            loaded = true;
        }
        return value;
    }

    public final void set(T value) {
        this.value = value;
        loaded = value != null;
    }

    public final void reset() {
        set(null);
    }

    protected abstract T load();
}
