package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Computable;

public interface Read {
    static <T> T call(ThrowableCallable<T, RuntimeException> callable) {
        return call(callable, null);
    }
    static <T> T call(ThrowableCallable<T, RuntimeException> callable, T defaultValue) {
        Application application = ApplicationManager.getApplication();
        return application.runReadAction((Computable<T>) () -> {
            try {
                return callable.call();
            } catch (ProcessCanceledException ignore) {
                return defaultValue;
            }
        });
    }

    static void run(ThrowableRunnable<RuntimeException> runnable) {
        Application application = ApplicationManager.getApplication();
        application.runReadAction(() -> {
            try {
                runnable.run();
            } catch (ProcessCanceledException ignore) {}
        });
    }
}
