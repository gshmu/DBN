package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.application.ApplicationManager;

public abstract class LazyResourceStatus {
    private long interval;
    private long lastCheck;
    private boolean value;
    private boolean checking;
    private boolean dirty;

    protected LazyResourceStatus(boolean initialValue, long interval){
        this.value = initialValue;
        this.interval = interval;
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
                        dirty = false;
                    }
                }
            }
        }

        return value;
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
            boolean oldValue = this.value;
            try {
                value = doCheck();
            } finally {
                checking = false;
                if (value != oldValue) statusChanged();
            }
        }
    }

    public abstract void statusChanged();

    public void set(boolean value) {
        this.value = value;
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean get() {
        return value;
    }

    protected abstract boolean doCheck();
}
