package com.dci.intellij.dbn.common.dispose;

public interface Disposable extends com.intellij.openapi.Disposable {
    boolean isDisposed();

    @Override
    void dispose();

    default void disposeInner() {}

    default void checkDisposed() {
        if (isDisposed()) throw AlreadyDisposedException.INSTANCE;
    }
}
