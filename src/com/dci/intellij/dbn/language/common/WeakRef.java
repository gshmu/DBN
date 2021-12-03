package com.dci.intellij.dbn.language.common;


import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Safe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class WeakRef<T> extends WeakReference<T> {
    protected WeakRef(T referent) {
        super(referent);
    }

    @Contract("null -> null;!null -> !null;")
    public static <T> WeakRef<T> of(@Nullable T element) {
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

    @Override
    public int hashCode() {
        T referent = get();
        return referent == null ? -1 : referent.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WeakRef) {
            WeakRef<?> that = (WeakRef<?>) obj;
            return Safe.equal(this, that, ref -> ref.get());
        }
        return false;
    }
}
