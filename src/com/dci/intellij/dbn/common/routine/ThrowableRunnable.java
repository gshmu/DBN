package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable> {
    void run() throws E;
}
