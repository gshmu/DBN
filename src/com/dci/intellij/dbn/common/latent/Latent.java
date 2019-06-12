package com.dci.intellij.dbn.common.latent;


import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.intellij.openapi.Disposable;

public interface Latent<T> {
    T get();
    T value();
    void set(T value);
    void reset();
    boolean loaded();

    static <T> BasicLatent<T> basic(Loader<T> loader) {
        return new BasicLatent<T>() {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }

    static <T extends Disposable, P extends RegisteredDisposable> DisposableLatent<T, P> disposable(P parent, Loader<T> loader) {
        return new DisposableLatent<T, P>(parent) {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }

    static <T, M> MutableLatent<T, M> mutable(Loader<M> mutableLoader, Loader<T> loader) {
        return new MutableLatent<T, M>() {
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

    static <T> WeakRefLatent<T> weak(Loader<T> loader) {
        return new WeakRefLatent<T>() {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }

    static <T> Latent<T> laf(Loader<T> loader) {
        BasicLatent<T> latent = basic(loader);
        Colors.subscribe(() -> latent.reset());
        return latent;
    }


    static <T> ThreadLocalLatent<T> thread(Loader<T> loader) {
        return new ThreadLocalLatent<T>(loader){};
    }

}
