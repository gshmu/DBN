package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ManagedCallable<R, E extends Throwable> {
    R call() throws E;

    @FunctionalInterface
    interface Unsafe<R> extends ManagedCallable<R, RuntimeException> {}
}
