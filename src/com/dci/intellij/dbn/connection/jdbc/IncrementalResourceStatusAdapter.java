package com.dci.intellij.dbn.connection.jdbc;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class IncrementalResourceStatusAdapter<T extends Resource> {
    private final ResourceStatus status;
    private final T resource;
    private AtomicInteger count = new AtomicInteger();

    public IncrementalResourceStatusAdapter(ResourceStatus status, T resource) {
        this.status = status;
        this.resource = resource;
    }

    public boolean set(ResourceStatus status, boolean value) {
        if (status == this.status) {
            int current = value ?
                    count.incrementAndGet() :
                    count.decrementAndGet();

            boolean changed = setInner(status, current > 0);
            if (changed) resource.statusChanged(status);
            return changed;
        } else {
            throw new IllegalArgumentException("Invalid resource status");
        }
    }

    protected abstract boolean setInner(ResourceStatus status, boolean value);


}
