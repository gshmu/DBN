package com.dci.intellij.dbn.common.util;

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
                        if (TimeUtil.isOlderThan(lastCheck, interval)) {
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

    protected abstract boolean doCheck();
}
