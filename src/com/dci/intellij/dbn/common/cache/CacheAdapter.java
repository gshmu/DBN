package com.dci.intellij.dbn.common.cache;

import org.jetbrains.annotations.NotNull;

public abstract class CacheAdapter<T, E extends Throwable> {
    private Cache cache;

    public CacheAdapter(@NotNull Cache cache) {
        this.cache = cache;
    }

    public final T get(String key) throws E {
        T value = cache.get(key);
        if (value == null) {
            synchronized (this) {
                value = cache.get(key);
                if (value == null) {
                    value = load();
                    cache.set(key, value);
                }
            }
        }
        return value;
    }

    protected abstract T load() throws E;
}
