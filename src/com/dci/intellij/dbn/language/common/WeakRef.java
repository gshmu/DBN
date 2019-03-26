package com.dci.intellij.dbn.language.common;


import com.dci.intellij.dbn.common.dispose.Failsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class WeakRef<T> extends WeakReference<T> {
    protected WeakRef(T referent) {
        super(referent);
    }

    @Nullable
    public static <T> WeakRef<T> from(@Nullable T element) {
        return element == null ? null : new WeakRef<T>(element);
    }

    @Nullable
    public static <T> T get(@Nullable WeakRef<T> ref) {
        return ref == null ? null : ref.get();
    }

    @Nullable
    @Override
    public T get() {
        return super.get();
    }

    @NotNull
    public T ensure() {
        return Failsafe.nn(get());
    }

    @Override
    public void clear() {
        super.clear();
    }
}
