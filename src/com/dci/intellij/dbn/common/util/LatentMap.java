package com.dci.intellij.dbn.common.util;

import gnu.trove.THashMap;

import java.util.Map;

public class LatentMap<K, V> {
    private Loader<K, V> loader;
    private Map<K, V> map = new THashMap<>();

    private LatentMap(Loader<K, V> loader) {
        this.loader = loader;
    }

    public static <K, V> LatentMap<K, V> create(Loader<K, V> loader) {
        return new LatentMap<K, V>(loader);
    }

    public V get(K key) {
        V value = map.get(key);
        if (value == null) {
            synchronized (this) {
                value = map.get(key);
                if (value == null) {
                    value= loader.load(key);
                    map.put(key, value);
                }
            }
        }
        return value;
    }

    @FunctionalInterface
    public interface Loader<K, V> {
        V load(K key);
    }
}
