package com.dci.intellij.dbn.common.ref;

import lombok.SneakyThrows;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface WeakRefCache<K, V> {
    V get(K key);

    V get(K key, Function<K, V> loader);

    V compute(K key, BiFunction<K, V, V> computer);

    void set(K key, V value);

    void remove(K key);

    static <K, V> WeakRefCache<K, V> build() {
        return new WeakRefCacheBasicImpl<>();
    }

    static <K, V> WeakRefCache<K, V> build(int maxSize) {
        return new WeakRefCacheGuavaImpl<>(maxSize);
    }
}
