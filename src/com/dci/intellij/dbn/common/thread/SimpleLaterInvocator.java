package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class SimpleLaterInvocator implements Runnable{
    private Object syncObject;

    public SimpleLaterInvocator(Object syncObject) {
        this.syncObject = syncObject;
    }

    public SimpleLaterInvocator() {
    }

    @Override
    public final void run() {
        if (syncObject == null) {
            execute();
        } else {
            synchronized (syncObject) {
                execute();
            }
        }
    }

    public abstract void execute();

    public void start() {
        ApplicationManager.getApplication().invokeLater(this/*, ModalityState.NON_MODAL*/);
    }
}
