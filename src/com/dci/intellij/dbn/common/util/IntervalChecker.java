package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.application.ApplicationManager;

public abstract class IntervalChecker {
    private long interval;
    private long lastCheck;
    private boolean value;
    private boolean checking;

    public IntervalChecker(long interval) {
        this.interval = interval;
    }

    public boolean check() {
        if (!checking) {
            synchronized (this) {
                if (!checking) {
                    checking = true;
                    try {
                        long currentTimeMillis = System.currentTimeMillis();
                        if (TimeUtil.isOlderThan(lastCheck, interval) && !ApplicationManager.getApplication().isDispatchThread()) {
                            lastCheck = currentTimeMillis;
                            value = doCheck();
                        }
                    } finally {
                        checking = false;
                    }
                }
            }
        }

        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    protected abstract boolean doCheck();
}
