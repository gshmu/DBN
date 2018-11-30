package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

public abstract class ConditionalLaterInvocator<T> extends SynchronizedTask<T>{
    protected ConditionalLaterInvocator() {}

    public final void start() {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            run();
        } else {
            application.invokeLater(this/*, ModalityState.NON_MODAL*/);
        }
    }

    @Override
    protected String getSyncKey() {
        return null;
    }

    public static ConditionalLaterInvocator create(Runnable runnable) {
        return new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                runnable.run();
            }
        };
    }

    public static void invoke(Runnable runnable) {
        ConditionalLaterInvocator.create(runnable).start();
    }
}