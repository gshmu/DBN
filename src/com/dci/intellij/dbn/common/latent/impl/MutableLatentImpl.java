package com.dci.intellij.dbn.common.latent.impl;


import com.dci.intellij.dbn.common.latent.Loader;
import com.dci.intellij.dbn.common.latent.RuntimeLatent;

public abstract class MutableLatentImpl<T, M> extends BasicLatentImpl<T, RuntimeException> implements RuntimeLatent<T> {
    private M mutable;

    protected MutableLatentImpl() {
        super();
    }

    protected abstract Loader<M, RuntimeException> getMutableLoader();


    @Override
    protected boolean shouldLoad(){
        return super.shouldLoad() ||
                mutable == null ||
                !mutable.equals(getMutableLoader().load());
    }

    @Override
    protected void beforeLoad() {
        mutable = getMutableLoader().load();
    }
}
