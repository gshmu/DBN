package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SimpleTask<T> extends AbstractTask<T>{
    protected SimpleTask() {
    }

    public void start() {
        run();
    }

    protected boolean canExecute() {
        return true;
    }

    public void run() {
        trace(this);
        try {
            if (canExecute()) {
                execute();
            } else {
                cancel();
            }
        } catch (ProcessCanceledException ignore) {
        }
    }

    protected abstract void execute();

    public static <T> SimpleTask<T> create(SimpleRunnable<T> runnable) {
        return new SimpleTask<T>() {
            @Override
            protected void execute() {
                runnable.run(getData());
            }
        };
    }

    @FunctionalInterface
    public interface SimpleRunnable<T> {
        void run(T data);
    }
}
