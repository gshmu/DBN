package com.dci.intellij.dbn.common.util;

@FunctionalInterface
public interface CustomRunnable<E extends Throwable> {
    void run() throws E;
}
