package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.util.Disposer;

public abstract class DisposableBase implements Disposable{
    private boolean disposed;

    public DisposableBase() {
    }

    public DisposableBase(Disposable parent) {
        if (parent != null) {
            Disposer.register(parent, this);
        }
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public final void dispose() {
        if (!disposed) {
            disposed = true;
            disposeInner();
        }
    }

    public void disposeInner(){};

    protected final void nullify() {
        DisposerUtil.nullify(this);
    }
}
