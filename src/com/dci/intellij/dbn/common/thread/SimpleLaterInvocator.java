package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class SimpleLaterInvocator extends SynchronizedTask{

    public void start() {
        ApplicationManager.getApplication().invokeLater(this, ApplicationManager.getApplication().getDefaultModalityState()/*, ModalityState.NON_MODAL*/);
    }

    @Override
    protected String getSyncKey() {
        return null;
    }
}
