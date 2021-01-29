package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.diagnostic.Logger;
import lombok.SneakyThrows;

public interface Measured {

    Logger LOGGER = LoggerFactory.createLogger();

    @SneakyThrows
    static void run(String identifier, ThrowableRunnable<Throwable> runnable) {
        long start = System.currentTimeMillis();
        try {
            runnable.run();
        } finally {
            log(identifier, start);
        }
    }

    @SneakyThrows
    static <T> T call(String identifier, ThrowableCallable<T, Throwable> callable) {
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } finally {
            log(identifier, start);
        }
    }

    static void log(String identifier, long start) {
        LOGGER.info("Measured execution: " + identifier + " - " + (System.currentTimeMillis() - start) + "ms");
    }
}
