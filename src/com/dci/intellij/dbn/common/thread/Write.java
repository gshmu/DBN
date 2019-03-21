package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

public interface Write {
    static void run(Runnable runnable) {
        Dispatch.conditional(() -> {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(
                    () -> Failsafe.guarded(() -> runnable.run()));
        });
    }
}
