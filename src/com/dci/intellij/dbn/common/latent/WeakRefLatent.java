package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.language.common.WeakRef;
import lombok.SneakyThrows;

abstract class WeakRefLatent<T> implements Latent<T>{
    private WeakRef<T> value;
    private volatile boolean loaded;

    WeakRefLatent() {}

    @SneakyThrows
    public final T get() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    T value = getLoader().load();
                    this.value = WeakRef.of(value);
                    loaded = true;
                }
            }
        }
        return WeakRef.get(value);
    }

    public abstract Loader<T> getLoader();

    public final void set(T value) {
        this.value = WeakRef.of(value);
        loaded = true;
    }

    public final boolean loaded() {
        return loaded;
    }

    public void reset() {
        value = null;
        loaded = false;
    }

    @Override
    public T value() {
        return WeakRef.get(value);
    }
}
