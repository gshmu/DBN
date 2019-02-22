package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

public abstract class ReadAction<T> {
    private boolean conditional;
    private ReadAction(boolean conditional) {
        this.conditional = conditional;
    }

    T start() {
        Application application = ApplicationManager.getApplication();
        if (conditional) {
            if (application.isReadAccessAllowed()) {
                return run();
            }
        }

        Computable<T> readAction = () -> ReadAction.this.run();
        return application.runReadAction(readAction);
    }

    abstract T run();

    public static <T> T invoke(boolean conditional, BasicCallable.Unsafe<T> callable) {
        return new ReadAction<T>(conditional) {
            @Override
            T run() {
                return callable.call();
            }

        }.start();
    }
}
