package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ManagedRunnable<E extends Throwable> {
    void run() throws E;

    @FunctionalInterface
    interface Unsafe extends ManagedRunnable<RuntimeException> {}
}
