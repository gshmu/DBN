package com.dci.intellij.dbn.common.latent;


public class MutableLatent<T, M> extends Latent<T> {
    private M mutable;
    private Loader<M> mutableLoader;

    private MutableLatent(Loader<T> loader, Loader<M> mutableLoader) {
        super(loader);
        this.mutableLoader = mutableLoader;
    }

    public static <T, M> MutableLatent<T, M> create(Loader<M> mutable, Loader<T> loader) {
        return new MutableLatent<>(loader, mutable);
    }

    @Override
    protected boolean shouldLoad() {
        return super.shouldLoad() ||
                mutable == null ||
                !mutable.equals(mutableLoader.load());
    }

    @Override
    protected void loading() {
        mutable = mutableLoader.load();
    }
}
