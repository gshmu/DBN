package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Computable;

public final class Read {
    private Read() {}

    public static <T> T call(ThrowableCallable<T, RuntimeException> callable) {
        return call(callable, null);
    }

    public static <T> T call(ThrowableCallable<T, RuntimeException> callable, T defaultValue) {
        return getApplication().runReadAction((Computable<T>) () -> {
            try {
                return callable.call();
            } catch (ProcessCanceledException ignore) {
                return defaultValue;
            }
        });
    }

    public static <T> T conditional(ThrowableCallable<T, RuntimeException> callable) {
        if (getApplication().isReadAccessAllowed()) {
            return callable.call();
        } else {
            return call(callable);
        }
    }


    public static <T> T conditional(ThrowableCallable<T, RuntimeException> callable, T defaultValue) {
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
