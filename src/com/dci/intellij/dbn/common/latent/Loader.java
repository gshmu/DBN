package com.dci.intellij.dbn.common.latent;

@FunctionalInterface
public interface Loader<T> {
    T load();
}
