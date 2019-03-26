package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.intellij.openapi.Disposable;

abstract class DisposableLatent<T extends Disposable, P extends RegisteredDisposable> extends BasicLatent<T> {
    private P parent;

    DisposableLatent(P parent) {
        super();
        this.parent = Failsafe.nd(parent);
    }

    @Override
    protected boolean shouldLoad() {
        return Failsafe.check(parent) && super.shouldLoad();
    }

    @Override
    public void loaded(T value) {
        super.loaded(value);
        Disposer.register(parent, value);
    }
}
