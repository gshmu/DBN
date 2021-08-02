package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.language.common.WeakRef;
import org.jetbrains.annotations.NotNull;

public abstract class LatentResourceStatus<T extends Property> {
    private final long interval;
    private long lastCheck;
    private boolean checking;
    private boolean dirty;
    private final T status;
    private WeakRef<PropertyHolder<T>> resource;

    protected LatentResourceStatus(PropertyHolder<T> resource, T status, boolean initialValue, long interval){
        resource.set(status, initialValue);
        this.status = status;
        this.interval = interval;
        this.resource = WeakRef.of(resource);
    }

    public synchronized boolean check() {
        if (!checking) {
            checking = true;
            long currentTimeMillis = System.currentTimeMillis();
            if (TimeUtil.isOlderThan(lastCheck, interval) || dirty) {
                lastCheck = currentTimeMillis;
                checkControlled();
            } else {
                checking = false;
            }
        }
        return get();
    }

    private void checkControlled() {
        if (ThreadMonitor.isDispatchThread()) {
            Background.run(() -> checkControlled());
        } else {
            boolean oldValue = get();
            try {
                set(doCheck());
            } finally {
                dirty = false;
                checking = false;
                if (get() != oldValue) statusChanged(status);
            }
        }
    }

    public abstract void statusChanged(T status);

    public void set(boolean value) {
        getResource().set(status, value);
    }

    @NotNull
    PropertyHolder<T> getResource() {
        return this.resource.ensure();
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean get() {
        return getResource().is(status);
    }

    protected abstract boolean doCheck();
}
