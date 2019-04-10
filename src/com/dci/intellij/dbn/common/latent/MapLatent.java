package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MapLatent<K, V> extends DisposableBase {
    private MapLoader<K, V> loader;
    private Map<K, V> map = new THashMap<>();

    private MapLatent(MapLoader<K, V> loader) {
        this.loader = loader;
    }

    public static <K, V> MapLatent<K, V> create(MapLoader<K, V> loader) {
        return new MapLatent<>(loader);
    }

    @Nullable
    public V value(K key) {
        return map.get(key);
    }

    @Nullable
    public V remove(K key) {
        return map.remove(key);
    }

    @Nullable
    public void put(K key, V value) {
        map.put(key, value);
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

    public void reset() {
        map.clear();
    }

    @Override
    public void disposeInner() {
        map.clear();
        super.disposeInner();
    }
}
