package com.dci.intellij.dbn.common.routine;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ParametricCallback<P> {
    void run(P parameter);

    @FunctionalInterface
    interface Unsafe<P> extends ParametricCallback<P> {};

    static void conditional(boolean condition, @Nullable Runnable runnable) {
        if (condition && runnable != null) {
            runnable.run();
        }
    }
}
