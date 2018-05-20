package com.dci.intellij.dbn.common.cache;

import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Cache {
    private Map<String, CacheValue> elements = new THashMap<String, CacheValue>();
    private int expiryTimeMillis = -1;

    public Cache(int expiryTimeMillis) {
        this.expiryTimeMillis = expiryTimeMillis;
    }

    @Nullable
    public <T> T get(String key) {
        CacheValue<T> cacheValue = elements.get(key);
        if (isValid(cacheValue)) {
            synchronized (this) {
                cacheValue = elements.get(key);
                if (isValid(cacheValue)) {
                    return cacheValue.getValue();
                }
            }
        }
        return null;
    }

    boolean isValid(CacheValue cacheValue) {
        return cacheValue != null && !cacheValue.isOlderThan(expiryTimeMillis);
    }

    public <T> void set(String key, T value) {
        elements.put(key, new CacheValue<T>(value));
    }
}
