package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;

public interface Unsafe {
    static void run(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw new UnsafeRuntimeException(e);
        }
    }

    static <T> T call(ThrowableCallable<T, Throwable> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw new UnsafeRuntimeException(e);
        }
    }

    class UnsafeRuntimeException extends RuntimeException {
        UnsafeRuntimeException(Throwable cause) {
            super(cause.getMessage(), cause);
        }
    }

    static Throwable cause(Throwable throwable) {
        if (throwable instanceof UnsafeRuntimeException) {
            return throwable.getCause();
        }
        return throwable;
    }

    static <T> T cast(Object o) {
        return (T) o;
    }

    static void silent(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ignore) {

        }
    }

}
