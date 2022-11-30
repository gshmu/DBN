package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.Disposable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public interface StatefulDisposable extends com.intellij.openapi.Disposable {

    default boolean isDisposed() {return false;}

    default void checkDisposed() {
        if (isDisposed()) throw AlreadyDisposedException.INSTANCE;
    }

    default void nullify() {
        Nullifier.nullify(this);
    }

    abstract class Base implements StatefulDisposable {
        @Getter
        private boolean disposed;

        public Base() {
        }

        public Base(@NotNull Disposable parent) {
            if (Checks.isValid(parent)) {
                Disposer.register(parent, this);
            }
        }

        @Override
        public final void dispose() {
            if (!disposed) {
                disposed = true;
                Unsafe.warned(() -> disposeInner());
            }
        }

        protected abstract void disposeInner();
    }
}
