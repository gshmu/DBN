package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Measured {
    private Measured() {}

    @SneakyThrows
    public static void run(String identifier, ThrowableRunnable<Throwable> runnable) {
        long start = System.currentTimeMillis();
        try {
            runnable.run();
        } finally {
            log(identifier, start);
        }
    }

    @SneakyThrows
    public static <T> T call(String identifier, ThrowableCallable<T, Throwable> callable) {
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } finally {
            log(identifier, start);
        }
    }

    public static void log(String identifier, long start) {
        log.info("Measured execution: " + identifier + " - " + (System.currentTimeMillis() - start) + "ms");
    }
}
