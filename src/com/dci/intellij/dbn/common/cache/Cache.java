package com.dci.intellij.dbn.common.cache;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private final Map<String, CacheValue> elements = new ConcurrentHashMap<>();
    private final String qualifier;
    private final long expiryMillis;


    public Cache(String qualifier, long expiryMillis) {
        this.qualifier = qualifier;
        this.expiryMillis = expiryMillis;
        CACHE_CLEANUP_TASK.register(this);
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
        return cacheValue != null && !cacheValue.isOlderThan(expiryMillis);
    }

    public void cleanup() {
        if (!elements.isEmpty()) {
            for (String key : elements.keySet()) {
                CacheValue cacheValue = elements.get(key);
                if (!isValid(cacheValue)) {
                    cacheValue = elements.remove(key);
                    Disposer.dispose(cacheValue.getValue());
                }
            }
        }
    }

    public void reset() {
        elements.clear();
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

    private static class ConnectionCacheCleanupTask extends TimerTask {
        List<WeakRef<Cache>> caches = ContainerUtil.createConcurrentList();

        @Override
        public void run() {
            for (WeakRef<Cache> cacheRef : caches) {
                Cache cache = cacheRef.get();
                if (cache == null) {
                    caches.remove(cacheRef);
                } else {
                    cache.cleanup();
                }
            }
        }

        void register(Cache cache) {
            caches.add(WeakRef.from(cache));
        }
    }

    private static final ConnectionCacheCleanupTask CACHE_CLEANUP_TASK = new ConnectionCacheCleanupTask();
    static {
        Timer poolCleaner = new Timer("DBN - Connection Cache Cleaner");
        poolCleaner.schedule(CACHE_CLEANUP_TASK, TimeUtil.Millis.THREE_MINUTES, TimeUtil.Millis.THREE_MINUTES);
    }
}
