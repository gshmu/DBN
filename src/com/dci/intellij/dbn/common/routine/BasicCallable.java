package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface BasicCallable<R, E extends Throwable> {
    R call() throws E;

    @FunctionalInterface
    interface Unsafe<R> extends BasicCallable<R, RuntimeException>{}
}
