package com.dci.intellij.dbn.common.routine;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

public abstract class ReadAction<T> implements BasicCallable<T> {
    T start() {
        Application application = ApplicationManager.getApplication();
        Computable<T> readAction = () -> ReadAction.this.call();
        return application.runReadAction(readAction);
    }

    public static <T> T invoke(BasicCallable<T> callable) {
        return new ReadAction<T>() {
            @Override
            public T call() {
                return callable.call();
            }

        }.start();
    }
}
