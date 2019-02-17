package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.Disposable;

abstract class DisposableLatent<T extends Disposable, P extends Disposable> extends BasicLatent<T> {
    private P parent;

    DisposableLatent(P parent) {
        super();
        this.parent = Failsafe.ensure(parent);
    }

    @Override
    protected boolean shouldLoad() {
        Failsafe.ensure(parent);
        return super.shouldLoad();
    }

    @Override
    public void loaded(T value) {
        super.loaded(value);
        DisposerUtil.register(parent, value);
    }
}
