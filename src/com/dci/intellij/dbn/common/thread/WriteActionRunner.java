package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class WriteActionRunner {
    private RunnableTask callback;

    public WriteActionRunner(RunnableTask callback) {
        this.callback = callback;
    }

    public WriteActionRunner() {
    }

    public void start() {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                Runnable writeAction = new Runnable() {
                    public void run() {
                        try {
                            WriteActionRunner.this.run();
                        } finally {
                            if (callback != null) {
                                callback.start();
                            }
                        }
                    }
                };
                ApplicationManager.getApplication().runWriteAction(writeAction);
            }
        }.start();
    }

    public abstract void run();

}
