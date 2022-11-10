package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ThrowableCallable<R, E extends Throwable> {
    R call() throws E;
}
