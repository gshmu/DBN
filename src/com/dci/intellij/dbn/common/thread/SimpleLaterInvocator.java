package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class SimpleLaterInvocator extends SimpleTask{
    private SimpleLaterInvocator(){}

    @Override
    @Deprecated
    public void start() {
        start(null);
    }

    public void start(ModalityState modalityState) {
        ApplicationManager.getApplication().invokeLater(this, CommonUtil.nvl(modalityState, ApplicationManager.getApplication().getDefaultModalityState())/*, ModalityState.NON_MODAL*/);
    }

    public static SimpleLaterInvocator create(Runnable runnable) {
        return new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                runnable.run();
            }
        };
    }

    public static void invoke(@NotNull DBNForm parentForm, Runnable runnable) {
        Failsafe.lenient(() -> {
            Failsafe.ensure(parentForm);
            invoke(parentForm.getComponent(), runnable);
        });
    }

    public static void invoke(@NotNull Component parentComponent, Runnable runnable) {
        ModalityState modalityState =
                ApplicationManager.getApplication().isDispatchThread() ?
                ModalityState.stateForComponent(parentComponent) :
                ModalityState.defaultModalityState();
        invoke(modalityState, runnable);
    }

    public static void invoke(ModalityState modalityState, Runnable runnable) {
        SimpleLaterInvocator.create(runnable).start(modalityState);
    }

    @Deprecated
    public static <T> void invoke(Runnable runnable) {
        SimpleLaterInvocator.create(runnable).start();
    }
}
