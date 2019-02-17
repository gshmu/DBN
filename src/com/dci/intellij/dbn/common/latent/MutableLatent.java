package com.dci.intellij.dbn.common.latent;


abstract class MutableLatent<T, M> extends BasicLatent<T> {
    private M mutable;

    MutableLatent() {
        super();
    }

    protected abstract Loader<M> getMutableLoader();


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
