package com.dci.intellij.dbn.common.latent.impl;


import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.Loader;
import lombok.SneakyThrows;

import java.util.Objects;

public abstract class MutableLatentImpl<T, M> extends BasicLatentImpl<T> implements Latent<T> {
    private M mutable;

    protected MutableLatentImpl() {
        super();
    }

    protected abstract Loader<M> getMutableLoader();


    @Override
    @SneakyThrows
    protected boolean shouldLoad(){
        return super.shouldLoad() ||
                mutable == null ||
                !Objects.equals(mutable, getMutableLoader().load());
    }

    @Override
    @SneakyThrows
    protected void beforeLoad() {
        mutable = getMutableLoader().load();
    }
}
