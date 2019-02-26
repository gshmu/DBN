package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

public interface Read {
    static <T> T call(Computable<T> callable) {
        Application application = ApplicationManager.getApplication();
        return application.runReadAction(callable);
    }

    static void run(Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        application.runReadAction(runnable);
    }
}
