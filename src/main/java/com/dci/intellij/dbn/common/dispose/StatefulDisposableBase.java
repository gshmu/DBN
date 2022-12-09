package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.Disposable;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public abstract class StatefulDisposableBase implements StatefulDisposable {
    @Getter
    private boolean disposed;

    public StatefulDisposableBase() {
    }

    public StatefulDisposableBase(@Nullable Disposable parent) {
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
