package com.dci.intellij.dbn.common.latent;

abstract class BasicLatent<T> implements Latent<T> {
    private T value;
    private boolean loaded;
    protected boolean loading;

    BasicLatent() {}

    public final T get() {
        if (shouldLoad()) {
            synchronized (this) {
                if (shouldLoad()) {
                    try {
                        loading = true;
                        loading();
                        Loader<T> loader = getLoader();
                        T newValue = loader == null ? null : loader.load();
                        if (value != newValue) {
                            value = newValue;
                        }
                        loaded(newValue);
                    } finally {
                        loading = false;
                    }
                }
            }
        }
        return value;
    }

    public abstract Loader<T> getLoader();

    protected void loading(){};

    protected boolean shouldLoad() {
        return !loaded;
    }

    public final void set(T value) {
        this.value = value;
        loaded = true;
    }

    public final boolean loaded() {
        return loaded;
    }

    public void loaded(T value) {
        loaded = true;
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
