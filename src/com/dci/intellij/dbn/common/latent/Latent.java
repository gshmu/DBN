package com.dci.intellij.dbn.common.latent;

public class Latent<T> {
    private T value;
    private Loader<T> loader;
    private boolean loaded;

    Latent(Loader<T> loader) {
        this.loader = loader;
    }

    public static <T> Latent<T> create(Loader<T> loader) {
        return new Latent<T>(loader);
    }

    public final T get() {
        if (shouldLoad()) {
            synchronized (this) {
                if (shouldLoad()) {
                    try {
                        loading();
                        value = loader.load();
                    } finally {
                        loaded(value);
                    }
                }
            }
        }
        return value;
    }

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


    @FunctionalInterface
    public interface Loader<T> {
        T load();
    }

}
