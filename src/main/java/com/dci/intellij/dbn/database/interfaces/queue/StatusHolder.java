package com.dci.intellij.dbn.database.interfaces.queue;

import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StatusHolder<T extends Enum<T>> {
    private final Stack<T> history = new Stack<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private T status;

    public StatusHolder(T initial) {
        this.status = initial;
    }

    public Stack<T> getHistory() {
        return history;
    }

    boolean change(T status) {
        return change(status, null);
    }

    boolean change(T status, Runnable runnable) {
        Lock lock = this.lock.writeLock();
        try {
            lock.lock();

            if (this.status.compareTo(status) >= 0) {
                return false;
            }

            this.history.push(this.status);
            this.status = status;
            if (runnable != null) runnable.run();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public T get() {
        Lock lock = this.lock.readLock();
        try {
            lock.lock();
            return status;
        } finally {
            lock.unlock();
        }
    }

    public boolean isBefore(T otherStatus) {
        return status.compareTo(otherStatus) < 0;
    }

    @Override
    public String toString() {
        return status.toString();
    }
}
