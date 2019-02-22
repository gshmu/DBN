package com.dci.intellij.dbn.common.routine;

import com.intellij.openapi.progress.ProgressIndicator;

@FunctionalInterface
public interface BackgroundRunnable<T> {
    void run(T data, ProgressIndicator progress);
}
