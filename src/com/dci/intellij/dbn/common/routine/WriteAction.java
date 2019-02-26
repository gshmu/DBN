package com.dci.intellij.dbn.common.routine;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

public abstract class WriteAction implements BasicRunnable{
    private WriteAction() {}

    void start() {
        Dispatch.invoke(() -> {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(() -> {
                WriteAction.this.run();
            });
        });
    }

    public static void invoke(java.lang.Runnable runnable) {
        new WriteAction() {
            @Override
            public void run() {
                Failsafe.lenient(() -> runnable.run());
            }
        }.start();
    }
}
