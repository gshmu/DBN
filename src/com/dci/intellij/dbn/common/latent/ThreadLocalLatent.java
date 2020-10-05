package com.dci.intellij.dbn.common.latent;

import lombok.SneakyThrows;

abstract class ThreadLocalLatent<T> implements Latent<T>{
    private final Loader<T> loader;
    private final ThreadLocal<T> value = new ThreadLocal<>();

    ThreadLocalLatent(Loader<T> loader) {
        this.loader = loader;
    }

    @Override
    public void set(T value) {
        this.value.set(value);
    }

    @Override
    public void reset() {
        this.value.set(null);
    }

    @Override
    public boolean loaded() {
        return this.value.get() != null;
    }

    @SneakyThrows
    public final T get(){
        T value = this.value.get();
        if (value == null) {
            value = loader.load();
            this.value.set(value);
        }
        return value;
    }

    @Override
    public T value() {
        return value.get();
    }
}
