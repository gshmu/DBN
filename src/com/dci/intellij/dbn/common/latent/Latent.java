package com.dci.intellij.dbn.common.latent;


import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.dci.intellij.dbn.common.latent.impl.BasicLatentImpl;
import com.dci.intellij.dbn.common.latent.impl.DisposableLatentImpl;
import com.dci.intellij.dbn.common.latent.impl.MutableLatentImpl;
import com.intellij.openapi.Disposable;

public interface Latent<T> {
    T get();
    T value();
    void set(T value);
    void reset();
    boolean loaded();

    static <T> Latent<T> basic(Loader<T> loader) {
        return new BasicLatentImpl<T>() {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }

    static <T extends Disposable, P extends RegisteredDisposable> DisposableLatentImpl<T, P> disposable(P parent, Loader<T> loader) {
        return new DisposableLatentImpl<T, P>(parent) {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }

    static <T, M> Latent<T> mutable(Loader<M> mutableLoader, Loader<T> loader) {
        return new MutableLatentImpl<T, M>() {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }

            @Override
            protected Loader<M> getMutableLoader() {
                return mutableLoader;
            }
        };
    }

    static <T> Latent<T> laf(Loader<T> loader) {
        Latent<T> latent = basic(loader);
        Colors.subscribe(() -> latent.reset());
        return latent;
    }

    static <T> WeakRefLatent<T> weak(Loader<T> loader) {
        return new WeakRefLatent<T>() {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }


    static <T> ThreadLocalLatent<T> thread(Loader<T> loader) {
        return new ThreadLocalLatent<T>(loader){};
    }

}
