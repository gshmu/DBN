package com.dci.intellij.dbn.common.util;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursivityGate {
    private final int maxIterations;
    private static final ThreadLocal<AtomicInteger> iterations = new ThreadLocal<>();

    public RecursivityGate(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    @SneakyThrows
    public <T> T call(Callable<T> resolver, Callable<T> defaultResolver) {
        AtomicInteger iterations = current();
        int currentIterations = iterations.get();
        if (currentIterations <= maxIterations) {
            try {
                iterations.incrementAndGet();
                return resolver.call();
            } finally {
                iterations.decrementAndGet();
            }
        }
        return defaultResolver.call();
    }

    @SneakyThrows
    public void run(Runnable task) {
        AtomicInteger iterations = current();
        int currentIterations = iterations.get();
        if (currentIterations <= maxIterations) {
            try {
                iterations.incrementAndGet();
                task.run();
            } finally {
                iterations.decrementAndGet();
            }
        }
    }

    @NotNull
    private static AtomicInteger current() {
        AtomicInteger current = iterations.get();
        if (current == null) {
            current = new AtomicInteger(0);
            iterations.set(current);
        }
        return current;
    }
}
