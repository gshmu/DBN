package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.dispose.FailsafeWeakRef;
import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

public abstract class LatentResourceStatus<T extends Property> {
    private long interval;
    private long lastCheck;
    private boolean checking;
    private boolean dirty;
    private T status;
    private FailsafeWeakRef<PropertyHolder<T>> resource;

    protected LatentResourceStatus(PropertyHolder<T> resource, T status, boolean initialValue, long interval){
        resource.set(status, initialValue);
        this.status = status;
        this.interval = interval;
        this.resource = new FailsafeWeakRef<>(resource);
    }

    public boolean check() {
        if (!checking) {
            synchronized (this) {
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
            }
        }

        return get();
    }

    private void checkControlled() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
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
        return this.resource.get();
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean get() {
        return getResource().is(status);
    }

    protected abstract boolean doCheck();
}
