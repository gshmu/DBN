package com.dci.intellij.dbn.diagnostics.data;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class DiagnosticEntry<T> {
    private final T identifier;
    private final AtomicLong invocationCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    private final AtomicLong timeoutCount = new AtomicLong();
    private final AtomicLong totalExecutionTime = new AtomicLong();
    private final AtomicLong bestExecutionTime = new AtomicLong();
    private final AtomicLong worstExecutionTime = new AtomicLong();
    private transient int signature;

    public DiagnosticEntry(T identifier) {
        this.identifier = identifier;
    }

    public long getInvocationCount() {
        return invocationCount.get();
    }

    public long getFailureCount() {
        return failureCount.get();
    }

    public long getTimeoutCount() {
        return timeoutCount.get();
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    public long getAverageExecutionTime() {
        return getTotalExecutionTime() / getInvocationCount();
    }

    public long getBestExecutionTime() {
        return bestExecutionTime.get();
    }

    public long getWorstExecutionTime() {
        return worstExecutionTime.get();
    }

    public void log(boolean failure, boolean timeout, long executionTime) {
        invocationCount.incrementAndGet();
        if (failure) {
            failureCount.incrementAndGet();
        }
        if (timeout) {
            timeoutCount.incrementAndGet();
        }

        if (bestExecutionTime.get() == 0 || bestExecutionTime.get() > executionTime) {
            bestExecutionTime.set(executionTime);
        }
        if (worstExecutionTime.get() == 0 || worstExecutionTime.get() < executionTime) {
            worstExecutionTime.set(executionTime);
        }

        totalExecutionTime.addAndGet(executionTime);
        signature = Objects.hash(
                identifier,
                invocationCount.get(),
                failureCount.get(),
                timeoutCount.get(),
                totalExecutionTime.get(),
                bestExecutionTime.get(),
                worstExecutionTime.get());
    }


}
