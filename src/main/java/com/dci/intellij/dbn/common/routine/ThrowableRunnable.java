package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable> {
    void run() throws E;

    default ThrowableCallable<?, E> asCallable() {
        return (ThrowableCallable<Object, E>) () -> {
            run();
            return null;
        };
    }
}
