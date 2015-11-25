package com.dci.intellij.dbn.common.util;

import java.sql.Timestamp;

public class ChangeTimestamp {
    private Timestamp value;
    private long captureTime;

    public ChangeTimestamp(Timestamp value) {
        this.value = value;
        this.captureTime = System.currentTimeMillis();
    }

    public Timestamp value() {
        return value;
    }

    public boolean isDirty() {
        return TimeUtil.isOlderThan(captureTime, 20 * TimeUtil.ONE_SECOND);
    }
}
