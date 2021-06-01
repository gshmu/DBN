package com.dci.intellij.dbn.diagnostics.data;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

@Getter
public class DiagnosticEntry {
    private final String identifier;
    private final AtomicLong invocationCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    private final AtomicLong timeoutCount = new AtomicLong();
    private final AtomicLong totalExecutionTime = new AtomicLong();

    public DiagnosticEntry(String identifier) {
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

    public void log(boolean failure, boolean timeout, long executionTime) {
        invocationCount.incrementAndGet();
        if (failure) failureCount.incrementAndGet();
        if (timeout) timeoutCount.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
    }


}
