package com.dci.intellij.dbn.common.cache;

public class CacheValue<T> {
    private T value;
    private long timestamp;

    CacheValue(T value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public T getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isOlderThan(int expiryTimeMillis) {
        return expiryTimeMillis > 0 && timestamp + expiryTimeMillis < System.currentTimeMillis();
    }
}
