package com.dci.intellij.dbn.common.dispose;

public interface Disposable extends com.intellij.openapi.Disposable {
    boolean isDisposed();

    default void checkDisposed() {
        if (isDisposed()) throw AlreadyDisposedException.INSTANCE;
    }
}
