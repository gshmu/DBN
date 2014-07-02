package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

public abstract class ConditionalLaterInvocator implements Runnable{
    private Object syncObject;

    public ConditionalLaterInvocator() {}
    public ConditionalLaterInvocator(Object syncObject) {
        this.syncObject = syncObject;
    }

    public void start() {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            run();
        } else {
            application.invokeLater(this/*, ModalityState.NON_MODAL*/);
        }
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
}