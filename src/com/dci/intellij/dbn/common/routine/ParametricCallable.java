package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ParametricCallable<P, R> {
    R call(P parameter);
}
