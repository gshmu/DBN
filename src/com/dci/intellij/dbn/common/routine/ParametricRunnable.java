package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ParametricRunnable<P, E extends Throwable> {
    void run(P parameter) throws E;

    interface Unsafe<P> extends ParametricRunnable<P, RuntimeException>{};
}
