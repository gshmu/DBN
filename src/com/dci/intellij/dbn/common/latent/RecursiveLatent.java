package com.dci.intellij.dbn.common.latent;

public abstract class RecursiveLatent<T> extends Latent<T>{
    private RecursiveLatent() {
        super();
    }

    @Override
    protected boolean shouldLoad() {
        return !loading && super.shouldLoad() ;
    }

    public static <T> RecursiveLatent<T> create(Loader<T> loader) {
        return new RecursiveLatent<T>() {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }
}
