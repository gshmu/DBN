package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.openapi.Disposable;

public class DisposableLatent<T extends Disposable, P extends Disposable> extends Latent<T> {
    private P parent;

    private DisposableLatent(P parent, Loader<T> loader) {
        super(loader);
        this.parent = FailsafeUtil.ensure(parent);
    }

    public static <T extends Disposable, P extends Disposable> DisposableLatent<T, P> create(P parent, Loader<T> loader) {
        return new DisposableLatent<>(parent, loader);
    }

    @Override
    protected boolean shouldLoad() {
        FailsafeUtil.ensure(parent);
        return super.shouldLoad();
    }

    @Override
    public void loaded(T value) {
        super.loaded(value);
        DisposerUtil.register(parent, value);
    }
}
