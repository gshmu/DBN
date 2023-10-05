package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProgressIndicator;

@FunctionalInterface
public interface ProgressRunnable {
    void run(ProgressIndicator progress);
}
