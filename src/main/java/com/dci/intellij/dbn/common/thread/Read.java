package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.Guarded;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
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
                return Guarded.call(defaultValue, callable);
            } catch (Throwable e) {
                log.error("Failed to perform read action. Returning default", e);
                return defaultValue;
            }
        });
    }

    public static <T> T conditional(Callable<T> callable) {
        if (isReadAccessAllowed()) {
            return Guarded.call(null, callable);
        } else {
            return call(callable);
        }
    }


    public static <T> T conditional(Callable<T> callable, T defaultValue) {
        if (isReadAccessAllowed()) {
            return Guarded.call(defaultValue, callable);
        } else {
            return call(callable, defaultValue);
        }
    }

    public static void run(Runnable runnable) {
        Application application = getApplication();
        application.runReadAction(() -> Guarded.run(runnable));
    }

    private static boolean isReadAccessAllowed() {
        return getApplication().isReadAccessAllowed();
    }

    private static Application getApplication() {
        return ApplicationManager.getApplication();
    }
}
