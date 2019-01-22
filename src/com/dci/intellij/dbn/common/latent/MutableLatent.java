package com.dci.intellij.dbn.common.latent;


public abstract class MutableLatent<T, M> extends Latent<T> {
    private M mutable;

    private MutableLatent() {
        super();
    }

    protected abstract Loader<M> getMutableLoader();


    public static <T, M> MutableLatent<T, M> create(Loader<M> mutableLoader, Loader<T> loader) {
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

    @Override
    protected boolean shouldLoad() {
        return super.shouldLoad() ||
                mutable == null ||
                !mutable.equals(getMutableLoader().load());
    }



    @Override
    protected void loading() {
        mutable = getMutableLoader().load();
    }
}
