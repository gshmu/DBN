package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

public abstract class ReadActionRunner<T> {
    private boolean conditional;
    private ReadActionRunner(boolean conditional) {
        this.conditional = conditional;
    }

    public final T start() {
        Application application = ApplicationManager.getApplication();
        if (conditional) {
            if (application.isReadAccessAllowed()) {
                return run();
            }
        }

        Computable<T> readAction = () -> ReadActionRunner.this.run();
        return application.runReadAction(readAction);
    }

    protected abstract T run();

    public static <T> T invoke(boolean conditional, BasicCallable.Unsafe<T> callable) {
        return new ReadActionRunner<T>(conditional) {
            @Override
            protected T run() {
                return callable.call();
            }

        }.start();
    }
}
