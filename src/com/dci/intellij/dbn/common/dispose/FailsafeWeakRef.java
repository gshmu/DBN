package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.language.common.WeakRef;
import org.jetbrains.annotations.NotNull;

public class FailsafeWeakRef<T> extends WeakRef<T> {
    public FailsafeWeakRef(T referent) {
        super(referent);
    }

    @NotNull
    @Override
    public T get() {
        return Failsafe.nn(super.get());
    }
}
