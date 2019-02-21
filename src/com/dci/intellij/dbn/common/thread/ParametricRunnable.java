package com.dci.intellij.dbn.common.thread;

@FunctionalInterface
public interface ParametricRunnable<P> {
    void run(P parameter);
}
