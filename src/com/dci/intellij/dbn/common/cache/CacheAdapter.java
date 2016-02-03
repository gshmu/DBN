package com.dci.intellij.dbn.common.cache;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

public abstract class CacheAdapter<T> {
    private Cache cache;

    public CacheAdapter(@NotNull Cache cache) {
        this.cache = cache;
    }

    public final T get(String key) throws SQLException {
        T value = cache.get(key);
        if (value == null) {
            value = load();
            cache.set(key, value);
        }
        return value;
    }

    protected abstract T load() throws SQLException;
}
