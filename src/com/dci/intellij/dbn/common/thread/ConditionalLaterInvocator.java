package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class ConditionalLaterInvocator<T> extends SimpleTask<T>{
    protected ConditionalLaterInvocator() {}

    @Override
    @Deprecated
    public final void start() {
        start(null);
    }

    public final void start(ModalityState modalityState) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            run();
        } else {
            if (modalityState == null) {
                application.invokeLater(this/*, ModalityState.NON_MODAL*/);
            } else {
                application.invokeLater(this, modalityState);
            }
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

    public static void invoke(@NotNull DBNForm parentForm, Runnable runnable) {
        FailsafeUtil.ensure(parentForm);
        invoke(parentForm.getComponent(), runnable);
    }

    public static void invoke(@NotNull Component parentComponent, Runnable runnable) {
        ModalityState modalityState = ModalityState.stateForComponent(parentComponent);
        invoke(modalityState, runnable);
    }

    public static void invoke(ModalityState modalityState, Runnable runnable) {
        ConditionalLaterInvocator.create(runnable).start(modalityState);
    }

    @Deprecated
    public static void invoke(Runnable runnable) {
        ConditionalLaterInvocator.create(runnable).start();
    }
}