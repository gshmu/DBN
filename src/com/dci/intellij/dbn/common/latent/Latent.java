package com.dci.intellij.dbn.common.latent;


import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.dci.intellij.dbn.common.latent.impl.BasicLatentImpl;
import com.dci.intellij.dbn.common.latent.impl.DisposableLatentImpl;
import com.dci.intellij.dbn.common.latent.impl.MutableLatentImpl;
import com.dci.intellij.dbn.common.latent.impl.RuntimeLatentImpl;
import com.intellij.openapi.Disposable;

public interface Latent<T, E extends Throwable> {
    T get() throws E;
    T value();
    void set(T value);
    void reset();
    boolean loaded();

    static <T, E extends Throwable> Latent<T, E> basic(Loader<T, E> loader) {
        return new BasicLatentImpl<T, E>() {
            @Override
            public Loader<T, E> getLoader() {
                return loader;
            }
        };
    }

    static <T> RuntimeLatent<T> runtime(Loader<T, RuntimeException> loader) {
        return new RuntimeLatentImpl<T>() {
            @Override
            public Loader<T, RuntimeException> getLoader() {
                return loader;
            }
        };
    }

    static <T extends Disposable, P extends RegisteredDisposable> DisposableLatentImpl<T, P> disposable(P parent, Loader<T, RuntimeException> loader) {
        return new DisposableLatentImpl<T, P>(parent) {
            @Override
            public Loader<T, RuntimeException> getLoader() {
                return loader;
            }
        };
    }

    static <T, M> RuntimeLatent<T> mutable(Loader<M, RuntimeException> mutableLoader, Loader<T, RuntimeException> loader) {
        return new MutableLatentImpl<T, M>() {
            @Override
            public Loader<T, RuntimeException> getLoader() {
                return loader;
            }

            @Override
            protected Loader<M, RuntimeException> getMutableLoader() {
                return mutableLoader;
            }
        };
    }

    static <T> RuntimeLatent<T> laf(Loader<T, RuntimeException> loader) {
        RuntimeLatent<T> latent = runtime(loader);
        Colors.subscribe(() -> latent.reset());
        return latent;
    }

    static <T> WeakRefLatent<T> weak(Loader<T, RuntimeException> loader) {
        return new WeakRefLatent<T>() {
            @Override
            public Loader<T, RuntimeException> getLoader() {
                return loader;
            }
        };
    }


    static <T> ThreadLocalLatent<T> thread(Loader<T, RuntimeException> loader) {
        return new ThreadLocalLatent<T>(loader){};
    }

}
