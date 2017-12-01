package com.dci.intellij.dbn.common.dispose;

public abstract class DisposableBase implements Disposable{
    private boolean disposed;

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
