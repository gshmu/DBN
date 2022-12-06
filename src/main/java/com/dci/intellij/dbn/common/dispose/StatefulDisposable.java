package com.dci.intellij.dbn.common.dispose;

public interface StatefulDisposable extends com.intellij.openapi.Disposable {

    default boolean isDisposed() {return false;}

    default void checkDisposed() {
        if (isDisposed()) throw AlreadyDisposedException.INSTANCE;
    }

    default void nullify() {
        Nullifier.nullify(this);
    }

}
