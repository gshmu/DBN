package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

public abstract class SimpleLaterInvocator extends SimpleTask{
    private SimpleLaterInvocator(){}

    @Override
    public void start() {
        start(null);
    }

    public void start(ModalityState modalityState) {
        Application application = ApplicationManager.getApplication();
        modalityState = CommonUtil.nvl(modalityState, application.getDefaultModalityState());
        application.invokeLater(this, modalityState/*, ModalityState.NON_MODAL*/);
    }

    public static SimpleLaterInvocator create(Runnable runnable) {
        return new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                runnable.run();
            }
        };
    }

    public static void invoke(ModalityState modalityState, Runnable runnable) {
        SimpleLaterInvocator.create(runnable).start(modalityState);
    }

    public static void invokeNonModal(Runnable runnable) {
        invoke(ModalityState.NON_MODAL, runnable);
    }

    public static <T> void invoke(Runnable runnable) {
        SimpleLaterInvocator.create(runnable).start();
    }
}
