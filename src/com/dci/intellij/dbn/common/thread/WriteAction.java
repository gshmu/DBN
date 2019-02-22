package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

public abstract class WriteAction {
    private WriteAction() {}

    void start() {
        SimpleLaterInvocator.invoke(() -> {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(() -> {
                WriteAction.this.run();
            });
        });
    }

    abstract void run();


    public static void invoke(Runnable runnable) {
        new WriteAction() {
            @Override
            void run() {
                runnable.run();
            }
        }.start();
    }
}
