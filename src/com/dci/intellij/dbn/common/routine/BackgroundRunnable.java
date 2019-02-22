package com.dci.intellij.dbn.common.routine;

import com.intellij.openapi.progress.ProgressIndicator;

@FunctionalInterface
public interface BackgroundRunnable<T, E extends Throwable> {
    void run(T data, ProgressIndicator progress) throws E;

    @FunctionalInterface
    interface Unsafe<P> extends BackgroundRunnable<P, RuntimeException>{};

}
