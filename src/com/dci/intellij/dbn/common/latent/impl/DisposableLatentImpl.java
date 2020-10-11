package com.dci.intellij.dbn.common.latent.impl;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.intellij.openapi.Disposable;

public abstract class DisposableLatentImpl<T extends Disposable, P extends RegisteredDisposable> extends BasicLatentImpl<T> {
    private final P parent;

    protected DisposableLatentImpl(P parent) {
        super();
        this.parent = Failsafe.nd(parent);
    }

    @Override
    protected boolean shouldLoad(){
        return Failsafe.check(parent) && super.shouldLoad();
    }

    @Override
    public void afterLoad(T value){
        super.afterLoad(value);
        Disposer.register(parent, value);
    }
}
