package com.dci.intellij.dbn.common.cache;

import org.jetbrains.annotations.NotNull;

public class CacheKey<T> {
    private String key;

    public CacheKey(@NotNull String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheKey<?> cacheKey = (CacheKey<?>) o;

        return key.equals(cacheKey.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
