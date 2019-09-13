package com.dci.intellij.dbn.common.latent;

@FunctionalInterface
public interface MapLoader<K, V, E extends Throwable> {
    V load(K key) throws E;
}
