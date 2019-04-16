package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProcessCanceledException;

public interface Dispatch {

    static void invoke(Runnable runnable) {
        invoke(null, runnable);
    }

    static void conditional(Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            try {
                runnable.run();
            } catch (ProcessCanceledException ignore) {

            }
        } else {
            invoke(null, runnable);
        }
    }

    static void invoke(ModalityState modalityState, Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        modalityState = CommonUtil.nvl(modalityState, application.getDefaultModalityState());
        application.invokeLater(
                () -> {
                    try {
                        runnable.run();
                    } catch (ProcessCanceledException ignore) {

                    }
                },
                modalityState/*, ModalityState.NON_MODAL*/);
    }

    static void invokeNonModal(Runnable runnable) {
        invoke(ModalityState.NON_MODAL, runnable);
    }
}
