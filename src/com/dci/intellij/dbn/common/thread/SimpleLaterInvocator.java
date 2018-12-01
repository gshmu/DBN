package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class SimpleLaterInvocator extends SynchronizedTask{
    private SimpleLaterInvocator(){}

    public void start() {
        ApplicationManager.getApplication().invokeLater(this, ApplicationManager.getApplication().getDefaultModalityState()/*, ModalityState.NON_MODAL*/);
    }

    @Override
    protected String getSyncKey() {
        return null;
    }

    public static SimpleLaterInvocator create(Runnable runnable) {
        return new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                runnable.run();
            }
        };
    }

    public static <T> void invoke(Runnable runnable) {
        SimpleLaterInvocator.create(runnable).start();
    }
}
