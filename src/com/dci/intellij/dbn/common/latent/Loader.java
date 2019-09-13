package com.dci.intellij.dbn.common.latent;

@FunctionalInterface
public interface Loader<T, E extends Throwable> {
    T load() throws E;
}
