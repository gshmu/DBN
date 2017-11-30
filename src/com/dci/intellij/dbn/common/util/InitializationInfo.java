package com.dci.intellij.dbn.common.util;

public class InitializationInfo {
    private long timestamp = System.currentTimeMillis();
    private Exception stack = new Exception("Initialization stack");

    public long getTimestamp() {
        return timestamp;
    }

    public Exception getStack() {
        return stack;
    }
}
