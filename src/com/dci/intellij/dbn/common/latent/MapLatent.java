package com.dci.intellij.dbn.common.latent;

import gnu.trove.THashMap;

import java.util.Map;

public class MapLatent<K, V> {
    private Loader<K, V> loader;
    private Map<K, V> map = new THashMap<>();

    private MapLatent(Loader<K, V> loader) {
        this.loader = loader;
    }

    public static <K, V> MapLatent<K, V> create(Loader<K, V> loader) {
        return new MapLatent<>(loader);
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
