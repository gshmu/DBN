package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProgressIndicator;

@FunctionalInterface
public interface BackgroundRunnable<T> {
    void run(T data, ProgressIndicator progress);
}
