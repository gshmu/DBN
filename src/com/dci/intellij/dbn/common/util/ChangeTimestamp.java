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
        return TimeUtil.isOlderThan(captureTime, 30 * TimeUtil.ONE_SECOND);
    }

    public boolean before(ChangeTimestamp changeTimestampCheck) {
        return value != null && changeTimestampCheck.value!= null && value.before(changeTimestampCheck.value);
    }
}
