package com.dci.intellij.dbn.common.util;

@FunctionalInterface
public interface CustomCallable<T, E extends Throwable> {
    T call() throws E;
}
