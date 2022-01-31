package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursivityGate {
    private final int maxPasses;
    private final ThreadLocal<AtomicInteger> passes = new ThreadLocal<>();

    public RecursivityGate() {
        this(1);
    }

    public RecursivityGate(int passes) {
        assert passes > 0;
        this.maxPasses = passes;
    }

    @SneakyThrows
    public <T> T call(Callable<T> resolver, Callable<T> defaultResolver) {
        AtomicInteger passes = current();
        if (passes.get() < maxPasses) {
            try {
                passes.incrementAndGet();
                return resolver.call();
            } catch (ProcessCanceledException ignore){
            } finally {
                passes.decrementAndGet();
            }
        }
        return defaultResolver == null ? null : defaultResolver.call();
    }

    @SneakyThrows
    public void run(Runnable task) {
        AtomicInteger passes = current();
        if (passes.get() < maxPasses) {
            try {
                passes.incrementAndGet();
                task.run();
            } catch (ProcessCanceledException ignore){
            } finally {
                passes.decrementAndGet();
            }
        }
    }

    @NotNull
    private AtomicInteger current() {
        AtomicInteger current = passes.get();
        if (current == null) {
            current = new AtomicInteger(0);
            passes.set(current);
        }
        return current;
    }
}
