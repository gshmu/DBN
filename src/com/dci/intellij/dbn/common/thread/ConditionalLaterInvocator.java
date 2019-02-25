package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@Deprecated
public abstract class ConditionalLaterInvocator<T> extends SimpleTask<T>{
    protected ConditionalLaterInvocator() {}

    @Override
    public final void start() {
        start(null);
    }

    public final void start(ModalityState modalityState) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            run();
        } else {
            modalityState = CommonUtil.nvl(modalityState, application.getDefaultModalityState());
            application.invokeLater(this, modalityState);
        }
    }

    public static ConditionalLaterInvocator create(Runnable runnable) {
        return new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                runnable.run();
            }
        };
    }

    @Deprecated
    public static void invoke(@NotNull DBNForm parentForm, Runnable runnable) {
        Failsafe.ensure(parentForm);
        invoke(parentForm.getComponent(), runnable);
    }

    @Deprecated
    public static void invoke(@NotNull Component parentComponent, Runnable runnable) {
        ModalityState modalityState = ModalityState.stateForComponent(parentComponent);
        invoke(modalityState, runnable);
    }

    public static void invoke(ModalityState modalityState, Runnable runnable) {
        ConditionalLaterInvocator.create(runnable).start(modalityState);
    }

    public static void invokeNonModal(Runnable runnable) {
        invoke(ModalityState.NON_MODAL, runnable);
    }

    public static void invoke(Runnable runnable) {
        ConditionalLaterInvocator.create(runnable).start();
    }
}