package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;

public abstract class SimpleTask<T> extends AbstractTask<T>{
    protected SimpleTask() {
    }

    @Override
    public void start() {
        run();
    }

    protected boolean canExecute() {
        return true;
    }

    @Override
    public void run() {
        trace(this);
        Failsafe.lenient(() -> {
            if (canExecute()) {
                execute();
            } else {
                cancel();
            }
        });
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
