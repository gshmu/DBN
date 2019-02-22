package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface BasicRunnable<E extends Throwable> {
    void run() throws E;

    @FunctionalInterface
    interface Unsafe extends BasicRunnable<RuntimeException>{}
}
