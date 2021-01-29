package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.SneakyThrows;

public interface Unsafe {
    Logger LOGGER = LoggerFactory.createLogger();

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

    static void warned(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore) {
        } catch (Throwable t) {
            String message = t.getMessage();
            LOGGER.warn(message == null ? t.getClass().getSimpleName() : message);
        }
    }

}
