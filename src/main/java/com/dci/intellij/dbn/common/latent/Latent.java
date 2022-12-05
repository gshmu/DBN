package com.dci.intellij.dbn.common.latent;


import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.latent.impl.BasicLatent;
import com.dci.intellij.dbn.common.latent.impl.MutableLatent;
import com.dci.intellij.dbn.common.latent.impl.ThreadLocalLatent;
import com.dci.intellij.dbn.common.latent.impl.WeakRefLatent;

public interface Latent<T> {
    T get();
    T value();
    void set(T value);
    void reset();
    boolean loaded();

    static <T> Latent<T> basic(Loader<T> loader) {
        return new BasicLatent<>(loader);
    }

    static <T, M> Latent<T> mutable(Loader<M> mutableLoader, Loader<T> loader) {
        return new MutableLatent<>(mutableLoader, loader);
    }

    static <T> WeakRefLatent<T> weak(Loader<T> loader) {
        return new WeakRefLatent<T>(loader);
    }

    static <T> Latent<T> laf(Loader<T> loader) {
        Latent<T> latent = basic(loader);
        Colors.subscribe(null, () -> latent.reset());
        return latent;
    }


    static <T> ThreadLocalLatent<T> thread(Loader<T> loader) {
        return new ThreadLocalLatent<T>(loader);
    }

}
