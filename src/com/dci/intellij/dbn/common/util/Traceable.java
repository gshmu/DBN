package com.dci.intellij.dbn.common.util;

public class Traceable {
    private static final ThreadLocal<Traceable> LOCAL = new ThreadLocal<Traceable>();
    private final Throwable trace;

    public Traceable() {
        Traceable traceable = LOCAL.get();
        Throwable trace = traceable == null ? new Throwable() : new Throwable(traceable.trace);
        this.trace = InternalApiUtil.getThrowableIntern(trace);
    }

    public Throwable getTrace() {
        return trace;
    }

    public static void trace(Traceable source) {
        LOCAL.set(source);
    }
}
