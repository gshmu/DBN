package com.dci.intellij.dbn.common.latent;

abstract class ThreadLocalLatent<T> implements RuntimeLatent<T>{
    private Loader<T, RuntimeException> loader;
    private ThreadLocal<T> value = new ThreadLocal<>();

    ThreadLocalLatent(Loader<T, RuntimeException> loader) {
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
