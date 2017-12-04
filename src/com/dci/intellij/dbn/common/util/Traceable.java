package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.util.objectTree.ThrowableInterner;

public class Traceable {
    private static final ThreadLocal<Traceable> LOCAL = new ThreadLocal<Traceable>();
    private Throwable trace;

    public Traceable() {
        Traceable traceable = LOCAL.get();
        Throwable trace = traceable == null ? new Throwable() : new Throwable(traceable.trace);
        this.trace = ThrowableInterner.intern(trace);
    }

    public Throwable getTrace() {
        return trace;
    }

    public static void trace(Traceable source) {
        LOCAL.set(source);
    }
}
