package com.dci.intellij.dbn.common.latent;

@FunctionalInterface
public interface MapLoader<K, V> {
    V load(K key);
}
