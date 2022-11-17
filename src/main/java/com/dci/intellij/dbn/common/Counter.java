package com.dci.intellij.dbn.common;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private final Set<Listener> listeners = new HashSet<>();
    private final AtomicInteger count = new AtomicInteger(0);

    public int increment() {
        int value = count.incrementAndGet();
        listeners.forEach(l -> l.onCount(value));
        return value;
    }

    public int decrement() {
        int value = count.decrementAndGet();
        listeners.forEach(l -> l.onCount(value));
        return value;
    }

    public int get() {
        return count.get();
    }

    public void reset() {
        count.set(0);
    }

    @Override
    public String toString() {
        return count.toString();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public interface Listener {
        void onCount(int value);
    }
}
