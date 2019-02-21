package com.dci.intellij.dbn.common.thread;

@FunctionalInterface
public interface ParametricCallable<P, R> {
    R call(P parameter);
}
