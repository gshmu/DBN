package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.util.UserDataHolderBase;
import org.jetbrains.annotations.Nullable;

public abstract class DisposableUserDataHolderBase extends UserDataHolderBase implements Disposable{
    private boolean disposed;

    public DisposableUserDataHolderBase() {
    }

    public DisposableUserDataHolderBase(@Nullable RegisteredDisposable parent) {
        if (parent != null) {
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
