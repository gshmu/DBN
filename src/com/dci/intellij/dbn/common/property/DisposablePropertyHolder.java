package com.dci.intellij.dbn.common.property;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.Disposer;

public abstract class DisposablePropertyHolder<T extends Property> extends PropertyHolderImpl<T> implements Disposable {
    private boolean disposed;

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

    public void disposeInner(){}

    protected final void nullify() {
        Disposer.nullify(this);
    }
}
