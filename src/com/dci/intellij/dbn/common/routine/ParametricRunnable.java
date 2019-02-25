package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ParametricRunnable<P> {
    void run(P parameter);
}
