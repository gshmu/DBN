package com.dci.intellij.dbn.connection.jdbc;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeWeakRef;
import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.application.ApplicationManager;

public abstract class LazyResourceStatus<T extends Property> {
    private long interval;
    private long lastCheck;
    private boolean checking;
    private boolean dirty;
    private T property;
    private FailsafeWeakRef<PropertyHolder<T>> resource;

    protected LazyResourceStatus(T property, PropertyHolder<T> resource, boolean initialValue, long interval){
        resource.set(property, initialValue);
        this.property = property;
        this.interval = interval;
        this.resource = new FailsafeWeakRef<PropertyHolder<T>>(resource);
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
            new SimpleBackgroundTask("check resource status") {
                @Override
                protected void execute() {
                    checkControlled();
                }
            }.start();
        } else {
            boolean oldValue = get();
            try {
                set(doCheck());
            } finally {
                dirty = false;
                checking = false;
                if (get() != oldValue) statusChanged();
            }
        }
    }

    public abstract void statusChanged();

    public void set(boolean value) {
        getResource().set(property, value);
    }

    @NotNull
    PropertyHolder<T> getResource() {
        return this.resource.get();
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean get() {
        return getResource().is(property);
    }

    protected abstract boolean doCheck();
}
