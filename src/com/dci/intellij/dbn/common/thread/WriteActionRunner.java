package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class WriteActionRunner {
    private RunnableTask callback;

    public WriteActionRunner(RunnableTask callback) {
        this.callback = callback;
    }

    protected WriteActionRunner() {
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

}
