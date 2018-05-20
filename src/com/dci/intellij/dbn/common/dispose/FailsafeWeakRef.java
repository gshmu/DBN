package com.dci.intellij.dbn.common.dispose;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class FailsafeWeakRef<T> extends WeakReference<T>{
    public FailsafeWeakRef(T referent) {
        super(referent);
    }

    @NotNull
    @Override
    public T get() {
        return FailsafeUtil.get(super.get());
    }
}
