package com.dci.intellij.dbn.common.property;

import com.dci.intellij.dbn.common.dispose.Disposable;

public abstract class DisposablePropertyHolder<T extends Property> extends PropertyHolderImpl<T> implements Disposable {
    private boolean disposed;

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
        }
    }
}
