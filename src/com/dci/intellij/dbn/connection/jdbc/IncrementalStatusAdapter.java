package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.language.common.WeakRef;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class IncrementalStatusAdapter<T, P extends Property.IntBase> {
    private final P status;
    private final WeakRef<T> resource;
    private final AtomicInteger count = new AtomicInteger();

    public IncrementalStatusAdapter(T resource, P status) {
        this.status = status;
        this.resource = WeakRef.of(resource);
    }

    public final boolean set(boolean value) {
        int current = value ?
                count.incrementAndGet() :
                count.decrementAndGet();

        boolean changed = setInner(status, current > 0);
        if (changed) statusChanged();
        return changed;
    }

    public T getResource() {
        return resource.ensure();
    }

    public P getStatus() {
        return status;
    }

    public AtomicInteger getCount() {
        return count;
    }

    protected abstract void statusChanged();

    protected abstract boolean setInner(P status, boolean value);
}
