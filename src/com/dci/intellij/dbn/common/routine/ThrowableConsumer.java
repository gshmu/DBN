package com.dci.intellij.dbn.common.routine;

@FunctionalInterface
public interface ThrowableConsumer<P, E extends Throwable> {
    void accept(P t) throws E;
}
