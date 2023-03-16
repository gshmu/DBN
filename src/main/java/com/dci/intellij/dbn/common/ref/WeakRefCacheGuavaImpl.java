package com.dci.intellij.dbn.common.ref;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

class WeakRefCacheGuavaImpl<K, V> implements WeakRefCache<K, V> {
    private final Cache<K, V> cache;

    WeakRefCacheGuavaImpl() {
        this(0);
    }

    WeakRefCacheGuavaImpl(int size) {
        CacheBuilder<K, V> builder = cast(CacheBuilder.newBuilder().weakKeys());
        if (size > 0) {
            builder.maximumSize(size).weakKeys();
        }

        cache = builder.build();
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    @SneakyThrows
    public V get(K key, Function<K, V> loader) {
        return cache.get(key, () -> loader.apply(key));
    }

    @Override
    public V compute(K key, BiFunction<K, V, V> loader) {
        V value = cache.getIfPresent(key);
        loader.apply(key, value);
        set(key, value);
        return value;
    }

    @Override
    @SneakyThrows
    public V computeIfAbsent(K key, Function<? super K, ? extends V> loader) {
        return cache.get(key, () -> loader.apply(key));
    }

    @Override
    public void set(K key, V value) {
        if (value == null)
            cache.invalidate(key); else
            cache.put(key, value);
    }

    @Override
    public V remove(K key) {
        V value = cache.getIfPresent(key);
        cache.invalidate(key);
        return value;
    }

}
