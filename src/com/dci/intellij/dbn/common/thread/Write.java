package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;

public interface Write {
    static void run(Runnable runnable) {
        Dispatch.invoke(() -> {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(
                    () -> {
                        try {
                            runnable.run();
                        } catch (ProcessCanceledException ignore) {}
                    });
        });
    }
}
