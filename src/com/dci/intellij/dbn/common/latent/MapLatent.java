package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapLatent<K, V, E extends Throwable> extends DisposableBase {
    private MapLoader<K, V, E> loader;
    private Map<K, V> map = ContainerUtil.newConcurrentMap();
    private AtomicInteger hitCount = new AtomicInteger();

    private MapLatent(MapLoader<K, V, E> loader) {
        this.loader = loader;
    }

    public static <K, V, E extends Throwable> MapLatent<K, V, E> create(MapLoader<K, V, E> loader) {
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

    public V get(K key) throws E {
        V value = map.get(key);
        if (value == null) {
            synchronized (this) {
                value = map.get(key);
                if (value == null) {
                    value= loader.load(key);
                    map.put(key, value);
                } else {
                    hitCount.incrementAndGet();
                }
            }
        } else {
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
        super.disposeInner();
    }
}
