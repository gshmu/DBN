package com.dci.intellij.dbn.common.thread;

@FunctionalInterface
public interface BasicRunnable<E extends Throwable> {
    void run() throws E;
}
