package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class ConditionalWriteActionRunner extends WriteActionRunner{

    @Override
    public void start() {
        if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
            run();
        } else {
            super.start();
        }
    }
}
