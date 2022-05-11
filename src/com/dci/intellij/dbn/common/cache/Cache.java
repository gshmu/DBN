package com.dci.intellij.dbn.common.cache;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.containers.ContainerUtil;
import lombok.SneakyThrows;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

public class Cache {
    private final Map<String, CacheValue> elements = new ConcurrentHashMap<>();
    private final long expiryMillis;


    public Cache(long expiryMillis) {
        this.expiryMillis = expiryMillis;
        CACHE_CLEANUP_TASK.register(this);
    }

    private boolean isValid(CacheValue cacheValue) {
        return cacheValue != null && !cacheValue.isOlderThan(expiryMillis);
    }

    private void cleanup() {
        if (!elements.isEmpty()) {
            for (val entry : elements.entrySet()) {
                String key = entry.getKey();
                CacheValue cacheValue = entry.getValue();
                if (!isValid(cacheValue)) {
                    cacheValue = elements.remove(key);
                    Object value = cacheValue.getValue();
                    if (value instanceof Disposable) {
                        Disposable disposable = (Disposable) value;
                        Disposer.dispose(disposable);
                    }

                }
            }
        }
    }

    public void reset() {
        elements.clear();
    }

    public <T, E extends Throwable> T get(String key, ThrowableCallable<T, E> loader) throws E {
        CacheValue cacheValue = elements.compute(key, (k, v) -> {
            if (!isValid(v)) {
                T value = load(loader);
                v = new CacheValue<T>(value);
            }
            return v;
        });
        return cast(cacheValue.getValue());
    }

    @SneakyThrows
    private <T, E extends Throwable> T load(ThrowableCallable<T, E> loader) {
        return loader.call();
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
            caches.add(WeakRef.of(cache));
        }
    }

    private static final ConnectionCacheCleanupTask CACHE_CLEANUP_TASK = new ConnectionCacheCleanupTask();
    static {
        Timer poolCleaner = new Timer("DBN - Connection Cache Cleaner");
        poolCleaner.schedule(CACHE_CLEANUP_TASK, TimeUtil.Millis.THREE_MINUTES, TimeUtil.Millis.THREE_MINUTES);
    }
}
