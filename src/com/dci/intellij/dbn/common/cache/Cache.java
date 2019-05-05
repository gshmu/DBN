package com.dci.intellij.dbn.common.cache;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Cache {
    private Map<String, CacheValue> elements = ContainerUtil.createSoftMap();
    private int expiryMillis;
    private String qualifier;


    public Cache(String qualifier, int expiryMillis) {
        this.qualifier = qualifier;
        this.expiryMillis = expiryMillis;
    }

    @Nullable
    private <T> T get(String key) {
        CacheValue<T> cacheValue = elements.get(key);
        if (isValid(cacheValue)) {
            return cacheValue.getValue();
        } else {
            cacheValue = elements.remove(key);
            if (cacheValue != null) {
                Disposer.dispose(cacheValue.getValue());
            }

        }
        return null;
    }

    private <T> void set(String key, T value) {
        elements.put(key, new CacheValue<T>(value));
    }

    private boolean isValid(CacheValue cacheValue) {
        return cacheValue != null && !cacheValue.isOlderThan(expiryMillis);
    }

    public <T, E extends Throwable> T get(String key, ThrowableCallable<T, E> loader) throws E {
        String syncKey = qualifier + "." + key;
        return Synchronized.call(syncKey, () -> {
            T value = get(key);
            if (value == null) {
                value = loader.call();
                set(key, value);
            }
            return value;
        });
    }
}
