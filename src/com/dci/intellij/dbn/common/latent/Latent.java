package com.dci.intellij.dbn.common.latent;

public abstract class Latent<T> {
    private T value;
    private boolean loaded;
    protected boolean loading;

    Latent() {}

    public static <T> Latent<T> create(Loader<T> loader) {
        return new Latent<T>() {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }

    public final T get() {
        if (shouldLoad()) {
            synchronized (this) {
                if (shouldLoad()) {
                    try {
                        loading = true;
                        loading();
                        T newValue = getLoader().load();
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

    public void reset() {
        value = null;
        loaded = false;
    }


}
