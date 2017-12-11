package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.util.Disposer;

public abstract class DisposableBase implements Disposable{
    private boolean disposed;

    public DisposableBase() {
    }

    public DisposableBase(Disposable parent) {
        Disposer.register(parent, this);
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public final void checkDisposed() {
        if (disposed) throw AlreadyDisposedException.INSTANCE;
    }
}
