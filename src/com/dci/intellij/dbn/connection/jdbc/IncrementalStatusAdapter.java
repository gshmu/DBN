package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.dispose.FailsafeWeakRef;
import com.dci.intellij.dbn.common.property.Property;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class IncrementalStatusAdapter<T, P extends Property> {
    private final P status;
    private final FailsafeWeakRef<T> resource;
    private AtomicInteger count = new AtomicInteger();

    public IncrementalStatusAdapter(T resource, P status) {
        this.status = status;
        this.resource = new FailsafeWeakRef<T>(resource);
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
        return resource.get();
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
