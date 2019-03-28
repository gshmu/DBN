package com.dci.intellij.dbn.common.dispose;

import org.jetbrains.annotations.Nullable;

public abstract class DisposableBase implements Disposable{
    private boolean disposed;

    public DisposableBase() {
    }

    public DisposableBase(@Nullable RegisteredDisposable parent) {
        if (Failsafe.check(parent)) {
            Disposer.register(parent, this);
        }
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public void markDisposed() {
        disposed = true;
    }

    @Override
    public final void dispose() {
        Disposable.super.dispose();
    }

    @Override
    public void disposeInner() {
        Disposable.super.disposeInner();
    }
}
