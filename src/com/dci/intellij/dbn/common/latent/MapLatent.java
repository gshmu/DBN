package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MapLatent<K, V> extends StatefulDisposable.Base {
    private final MapLoader<K, V> loader;
    private final Map<K, V> map = new ConcurrentHashMap<>();
    private final AtomicInteger hitCount = new AtomicInteger();

    private MapLatent(MapLoader<K, V> loader) {
        this.loader = loader;
    }

    public static <K, V, E extends Throwable> MapLatent<K, V> create(MapLoader<K, V> loader) {
        return new MapLatent<>(loader);
    }

    @Nullable
    public V value(K key) {
        return map.get(key);
    }

    @Nullable
    public V removeKey(K key) {
        return map.remove(key);
    }

    public void removeValue(V value) {
        map.values().removeIf(v -> v == value);
    }

    public void put(K key, V value) {
        map.put(key, value);
    }

    public Collection<V> values() {
        return map.values();
    }

    public V get(K key) {
        AtomicBoolean computed = new AtomicBoolean(false);
        V value = map.computeIfAbsent(key, k -> {
            computed.set(true);
            return loader.load(k);
        });

        if (!computed.get()) {
            hitCount.incrementAndGet();
        }
        return value;
    }

    public void reset() {
        map.clear();
    }

    public int hitCount() {
        return hitCount.intValue();
    }

    @Override
    public void disposeInner() {
        map.clear();
    }
}
