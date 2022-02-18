package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Computable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public final class Read {
    private Read() {}

    public static <T> T call(Callable<T> callable) {
        return call(callable, null);
    }

    public static <T> T call(Callable<T> callable, T defaultValue) {
        return getApplication().runReadAction((Computable<T>) () -> {
            try {
                return callable.call();
            } catch (ProcessCanceledException ignore) {
                return defaultValue;
            } catch (Throwable e) {
                log.error("Failed to perform read action. Returning default", e);
                return defaultValue;
            }
        });
    }

    @SneakyThrows
    public static <T> T conditional(Callable<T> callable) {
        if (getApplication().isReadAccessAllowed()) {
            return callable.call();
        } else {
            return call(callable);
        }
    }


    @SneakyThrows
    public static <T> T conditional(Callable<T> callable, T defaultValue) {
        if (getApplication().isReadAccessAllowed()) {
            try {
                return callable.call();
            } catch (ProcessCanceledException e) {
                return defaultValue;
            }
        } else {
            return call(callable, defaultValue);
        }
    }

    public static void run(Runnable runnable) {
        Application application = getApplication();
        application.runReadAction(() -> {
            try {
                runnable.run();
            } catch (ProcessCanceledException ignore) {}
        });
    }


    private static Application getApplication() {
        return ApplicationManager.getApplication();
    }
}
