package com.dci.intellij.dbn.common.cache;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class CacheKey<T> {
    private final String key;

    public CacheKey(@NotNull String key) {
        this.key = key.intern();
    }
}
