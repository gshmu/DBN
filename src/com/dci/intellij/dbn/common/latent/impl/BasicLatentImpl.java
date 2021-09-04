package com.dci.intellij.dbn.common.latent.impl;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.Loader;
import lombok.SneakyThrows;

public abstract class BasicLatentImpl<T> implements Latent<T> {
    private T value;
    private volatile boolean loaded;
    protected volatile boolean loading;

    protected BasicLatentImpl() {}

    @SneakyThrows
    public final T get(){
        if (shouldLoad()) {
            synchronized (this) {
                if (shouldLoad()) {
                    try {
                        loading = true;
                        beforeLoad();
                        Loader<T> loader = getLoader();
                        T newValue = loader == null ? null : loader.load();
                        if (value != newValue) {
                            value = newValue;
                        }
                        afterLoad(newValue);
                    } finally {
                        loading = false;
                    }
                }
            }
        }
        return value;
    }

    public abstract Loader<T> getLoader();

    protected boolean shouldLoad() {
        return !loaded;
    }

    protected void beforeLoad() {};

    protected void afterLoad(T value) {
        loaded = true;
    }

    public final void set(T value) {
        this.value = value;
        loaded = true;
    }

    public final boolean loaded() {
        return loaded;
    }

    @Override
    public T value() {
        return value;
    }

    public void reset() {
        value = null;
        loaded = false;
    }
}
