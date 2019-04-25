package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.Nullable;

public interface Read {
    @Nullable
    static <T> T call(ThrowableCallable<T, RuntimeException> callable) {
        Application application = ApplicationManager.getApplication();
        return application.runReadAction((Computable<T>) () -> {
            try {
                return callable.call();
            } catch (ProcessCanceledException ignore) {
                return null;
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
