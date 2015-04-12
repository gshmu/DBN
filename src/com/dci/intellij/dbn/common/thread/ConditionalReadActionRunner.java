package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Computable;

public abstract class ConditionalReadActionRunner<T> {

    public final T start() {
        Application application = ApplicationManager.getApplication();
        if (application.isReadAccessAllowed()) {
            try {
                return run();
            } catch (ProcessCanceledException e) {
                return null;
            }
        } else {
            Computable<T> readAction = new Computable<T>() {
                @Override
                public T compute() {
                    try {
                        return ConditionalReadActionRunner.this.run();
                    } catch (ProcessCanceledException e) {
                        return null;
                    }

                }
            };
            return ApplicationManager.getApplication().runReadAction(readAction);
        }
    }

    protected abstract T run();

}
