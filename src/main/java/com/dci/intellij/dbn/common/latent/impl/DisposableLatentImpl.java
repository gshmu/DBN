package com.dci.intellij.dbn.common.latent.impl;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.intellij.openapi.Disposable;

import static com.dci.intellij.dbn.common.dispose.Checks.isValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;

public abstract class DisposableLatentImpl<T extends Disposable, P extends Disposable> extends BasicLatentImpl<T> {
    private final P parent;

    protected DisposableLatentImpl(P parent) {
        super();
        this.parent = nd(parent);
    }

    @Override
    protected boolean shouldLoad(){
        return isValid(parent) && super.shouldLoad();
    }

    @Override
    public void afterLoad(T value){
        super.afterLoad(value);
        Disposer.register(parent, value);
    }
}
