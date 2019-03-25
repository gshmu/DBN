package com.dci.intellij.dbn.common.dispose;

public interface Disposable extends com.intellij.openapi.Disposable {
    boolean isDisposed();
    void markDisposed();

    @Override
    default void dispose() {
        if (!isDisposed()) {
            markDisposed();
            disposeInner();
        }
    }

    default void disposeInner() {
        if (Disposer.isNullifiable(this)) {
            Disposer.nullify(this);
        }
    }

    default void checkDisposed() {
        if (isDisposed()) throw AlreadyDisposedException.INSTANCE;
    }
}
