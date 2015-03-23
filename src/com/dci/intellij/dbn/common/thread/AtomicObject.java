package com.dci.intellij.dbn.common.thread;

public class AtomicObject<T> {
    public  T object;

    public void set(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }
}
