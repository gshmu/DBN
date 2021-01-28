package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import lombok.SneakyThrows;

public interface Unsafe {

    @SneakyThrows
    static void run(ThrowableRunnable<Throwable> runnable) {
        runnable.run();
    }

    @SneakyThrows
    static <T> T call(ThrowableCallable<T, Throwable> callable) {
        return callable.call();
    }

    static <T> T cast(Object o) {
        return (T) o;
    }

    static void silent(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (Throwable ignore) {

        }
    }

}
