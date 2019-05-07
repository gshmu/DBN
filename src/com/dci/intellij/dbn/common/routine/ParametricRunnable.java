package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ParametricRunnable<P, E extends Throwable> {
    void run(P parameter) throws E;

    interface Basic<P> extends ParametricRunnable<P, RuntimeException> {}
}
