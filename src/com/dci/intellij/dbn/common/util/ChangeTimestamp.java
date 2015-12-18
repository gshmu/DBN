package com.dci.intellij.dbn.common.util;

import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;

public class ChangeTimestamp {
    private Timestamp value;
    private long captureTime;

    public ChangeTimestamp() {
        this.captureTime = System.currentTimeMillis();
        this.value = new Timestamp(captureTime);
    }
    public ChangeTimestamp(@NotNull Timestamp value) {
        this.value = value;
        this.captureTime = System.currentTimeMillis();
    }

    @NotNull
    public Timestamp value() {
        return value;
    }

    public boolean isDirty() {
        return TimeUtil.isOlderThan(captureTime, 30 * TimeUtil.ONE_SECOND);
    }

    public boolean isOlderThan(ChangeTimestamp changeTimestampCheck) {
        return value.before(changeTimestampCheck.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
