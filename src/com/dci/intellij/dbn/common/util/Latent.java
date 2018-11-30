package com.dci.intellij.dbn.common.util;

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
                    initLoad();
                    value = loader.load();
                    loaded = true;
                }
            }
        }
        return value;
    }

    protected void initLoad(){};

    protected boolean shouldLoad() {
        return !loaded;
    }

    public final void set(T value) {
        this.value = value;
        loaded = true;
    }

    public final boolean loaded() {
        return !shouldLoad();
    }


    @FunctionalInterface
    public interface Loader<T> {
        T load();
    }

}
