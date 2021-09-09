package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Unsafe {
    public Unsafe() {}

    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static void silent(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (Throwable ignore) {
        }
    }

    public static <T> T silent(T defaultValue, ThrowableCallable<T, Throwable> callable) {
        try {
            return callable.call();
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static void warned(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore) {
        } catch (Throwable t) {
            String message = t.getMessage();
            log.warn(message == null ? t.getClass().getSimpleName() : message);
        }
    }

    public static <T> T warned(T defaultValue, ThrowableCallable<T, Throwable> callable) {
        try {
            return callable.call();
        } catch (ProcessCanceledException ignore) {
        } catch (Throwable t) {
            String message = t.getMessage();
            log.warn(message == null ? t.getClass().getSimpleName() : message);
        }
        return defaultValue;
    }

}
