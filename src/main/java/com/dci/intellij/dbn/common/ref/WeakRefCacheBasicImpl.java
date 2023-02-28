package com.dci.intellij.dbn.common.ref;

import com.intellij.util.containers.ContainerUtil;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

class WeakRefCacheBasicImpl<K, V> implements WeakRefCache<K, V> {
    private final Map<K, V> cache = ContainerUtil.createConcurrentWeakMap();

    WeakRefCacheBasicImpl() {
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    @SneakyThrows
    public V get(K key, Function<K, V> loader) {
        return cache.computeIfAbsent(key, loader);
    }

    @Override
    public V compute(K key, BiFunction<K, V, V> computer) {
        return cache.compute(key, computer);
    }


    @Override
    public void set(K key, @Nullable V value) {
        if (value == null)
            cache.remove(key); else
            cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

}
