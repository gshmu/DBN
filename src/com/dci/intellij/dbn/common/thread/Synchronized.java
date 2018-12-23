package com.dci.intellij.dbn.common.thread;

public interface Synchronized {
    static void run(Object syncObject, Condition<Boolean> condition, Runnable runnable) {
        if(condition.evaluate()) {
            synchronized (syncObject) {
                if(condition.evaluate()) {
                    runnable.run();
                }
            }
        }
    }

    @FunctionalInterface
    public interface Condition<T> {
        T evaluate();
    }
}
