package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;

@Slf4j
public final class Unsafe {
    public Unsafe() {}

    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static boolean silent(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static <P> boolean silent(P param, ParametricRunnable<P, Throwable> runnable) {
        try {
            runnable.run(param);
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static <R> R silent(R defaultValue, ThrowableCallable<R, Throwable> callable) {
        try {
            return callable.call();
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static <P, R> R silent(R defaultValue, P param, ParametricCallable<P, R, Throwable> callable) {
        try {
            return callable.call(param);
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static void warned(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (ProcessCanceledException e) {
            conditionallyLog(e);
        } catch (Throwable t) {
            String message = t.getMessage();
            log.warn(message == null ? t.getClass().getSimpleName() : message);
        }
    }

    public static <T> T warned(T defaultValue, ThrowableCallable<T, Throwable> callable) {
        try {
            return callable.call();
        } catch (ProcessCanceledException e) {
            conditionallyLog(e);
        } catch (Throwable t) {
            String message = t.getMessage();
            log.warn(message == null ? t.getClass().getSimpleName() : message);
        }
        return defaultValue;
    }

}
