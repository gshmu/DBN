package com.dci.intellij.dbn.common.thread;

@FunctionalInterface
public interface BasicCallable<R, E extends Throwable> {
    R call() throws E;
}
