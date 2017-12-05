package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
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
                    long currentTimeMillis = System.currentTimeMillis();
                    if (TimeUtil.isOlderThan(lastCheck, interval)) {
                        lastCheck = currentTimeMillis;
                        checkControlled();
                    } else {
                        checking = false;
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
                    try {
                        value = doCheck();
                    } finally {
                        checking = false;
                    }
                }
            }.start();
        } else {
            try {
                value = doCheck();
            } finally {
                checking = false;
            }
        }

    }

    public void set(boolean value) {
        this.value = value;
    }

    protected abstract boolean doCheck();
}
