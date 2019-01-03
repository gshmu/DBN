package com.dci.intellij.dbn.common.thread;

public interface Synchronized {
    static <E extends Throwable> void run(Object syncObject, Condition<Boolean> condition, BasicRunnable<E> runnable) throws E {
        if(condition.evaluate()) {
            synchronized (syncObject) {
                if(condition.evaluate()) {
                    runnable.run();
                }
            }
        }
    }

    @FunctionalInterface
    interface Condition<T> {
        T evaluate();
    }
}
