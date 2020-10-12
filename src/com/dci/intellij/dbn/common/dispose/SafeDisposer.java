package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SafeDisposer {
    static void register(@Nullable Disposable parent, @NotNull Disposable disposable) {
        if (Failsafe.check(parent)) {
            Disposer.register(parent, disposable);
        }
    }

    static void dispose(@Nullable Disposable disposable) {
        if (Failsafe.check(disposable)) {
            Disposer.dispose(disposable);
        }
    }

    static <T> T replace(T oldElement, T newElement) {
        DisposeUtil.disposeInBackground(oldElement);
        return newElement;
    }
}
