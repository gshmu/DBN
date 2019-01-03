package com.dci.intellij.dbn.common.latent;

public class ThreadLocalLatent<T> {
    private Loader<T> loader;
    private ThreadLocal<T> value = new ThreadLocal<>();

    private ThreadLocalLatent(Loader<T> loader) {
        this.loader = loader;
    }

    public static <T> ThreadLocalLatent<T> create(Loader<T> loader) {
        return new ThreadLocalLatent<T>(loader);
    }

    public final T get() {
        T value = this.value.get();
        if (value == null) {
            value = loader.load();
            this.value.set(value);
        }
        return value;
    }
}
