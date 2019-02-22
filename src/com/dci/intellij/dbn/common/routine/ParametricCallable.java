package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ParametricCallable<P, R, E extends Throwable> {
    R call(P parameter);

    interface Unsafe<P, R> extends ParametricCallable<P, R, RuntimeException> {}
}
