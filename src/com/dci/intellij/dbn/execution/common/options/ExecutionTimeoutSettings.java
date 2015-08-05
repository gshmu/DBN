package com.dci.intellij.dbn.execution.common.options;

public interface ExecutionTimeoutSettings {
    int getDebugExecutionTimeout();

    int getExecutionTimeout();

    boolean setExecutionTimeout(int timeout);

    boolean setDebugExecutionTimeout(int timeout);
}
