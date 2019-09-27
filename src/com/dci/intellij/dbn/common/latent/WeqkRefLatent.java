package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.language.common.WeakRef;

abstract class WeakRefLatent<T> implements RuntimeLatent<T>{
    private WeakRef<T> valueRef;
    private boolean loaded;

    WeakRefLatent() {}

    public final T get() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    T value = getLoader().load();
                    valueRef = WeakRef.from(value);
                    loaded = true;
                }
            }
        }
        return WeakRef.get(valueRef);
    }

    public abstract Loader<T, RuntimeException> getLoader();

    public final void set(T value) {
        this.valueRef = WeakRef.from(value);
        loaded = true;
    }

    public final boolean loaded() {
        return loaded;
    }

    public void reset() {
        valueRef = null;
        loaded = false;
    }

    @Override
    public T value() {
        return WeakRef.get(valueRef);
    }
}
