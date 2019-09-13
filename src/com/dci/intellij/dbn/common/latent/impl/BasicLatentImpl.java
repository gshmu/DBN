package com.dci.intellij.dbn.common.latent.impl;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.Loader;

public abstract class BasicLatentImpl<T, E extends Throwable> implements Latent<T, E> {
    private T value;
    private boolean loaded;
    protected boolean loading;

    protected BasicLatentImpl() {}

    public final T get() throws E{
        if (shouldLoad()) {
            synchronized (this) {
                if (shouldLoad()) {
                    try {
                        loading = true;
                        beforeLoad();
                        Loader<T, E> loader = getLoader();
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

    public abstract Loader<T, E> getLoader();

    protected boolean shouldLoad() throws E {
        return !loaded;
    }

    protected void beforeLoad() throws E {};

    protected void afterLoad(T value)throws E {
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
