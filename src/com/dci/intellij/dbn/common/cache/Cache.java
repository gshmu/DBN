package com.dci.intellij.dbn.common.cache;

import com.dci.intellij.dbn.common.routine.ManagedCallable;
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
    private <T> T get(String key) {
        CacheValue<T> cacheValue = elements.get(key);
        if (isValid(cacheValue)) {
            return cacheValue.getValue();
        }
        return null;
    }

    private <T> void set(String key, T value) {
        elements.put(key, new CacheValue<T>(value));
    }

    private boolean isValid(CacheValue cacheValue) {
        return cacheValue != null && !cacheValue.isOlderThan(expiryTimeMillis);
    }

    public <T, E extends Throwable> T get(String key, ManagedCallable<T, E> loader) throws E {
        T value = get(key);
        if (value == null) {
            synchronized (this) {
                value = get(key);
                if (value == null) {
                    value = loader.call();
                    set(key, value);
                }
            }
        }
        return value;
    }
}
