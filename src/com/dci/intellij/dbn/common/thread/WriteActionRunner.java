package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class WriteActionRunner {
    private RunnableTask callback;

    private WriteActionRunner(RunnableTask callback) {
        this.callback = callback;
    }

    private WriteActionRunner() {
    }

    public void start() {
        SimpleLaterInvocator.invoke(() -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    WriteActionRunner.this.run();
                } finally {
                    if (callback != null) {
                        callback.start();
                    }
                }
            });
        });
    }

    public abstract void run();


    public static void invoke(Runnable runnable) {
        new WriteActionRunner() {
            @Override
            public void run() {
                runnable.run();
            }
        }.start();
    }

    public static void invoke(Runnable runnable, RunnableTask callback) {
        new WriteActionRunner(callback) {
            @Override
            public void run() {
                runnable.run();
            }
        }.start();
    }

}
