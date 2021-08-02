package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.Safe;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProcessCanceledException;

import java.util.concurrent.atomic.AtomicReference;

public interface Dispatch {

    static void run(Runnable runnable) {
        run(null, runnable);
    }

    static void runConditional(Runnable runnable) {
        if (ThreadMonitor.isDispatchThread()) {
            try {
                runnable.run();
            } catch (ProcessCanceledException ignore) {}
        } else {
            run(null, runnable);
        }
    }

    static void run(ModalityState modalityState, Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        modalityState = CommonUtil.nvl(modalityState, application.getDefaultModalityState());
        application.invokeLater(() -> Safe.run(runnable), modalityState/*, ModalityState.NON_MODAL*/);
    }

    static void invokeNonModal(Runnable runnable) {
        run(ModalityState.NON_MODAL, runnable);
    }

    static <T, E extends Throwable> T callConditional(ThrowableCallable<T, E> callable) throws E{
        if (ThreadMonitor.isDispatchThread()) {
            return callable.call();
        } else {
            return call(callable);
        }
    }


    static <T, E extends Throwable> T call(ThrowableCallable<T, E> callable) throws E{
        Application application = ApplicationManager.getApplication();
        ModalityState modalityState = application.getDefaultModalityState();
        AtomicReference<T> resultRef = new AtomicReference<>();
        AtomicReference<E> exceptionRef = new AtomicReference<>();
        application.invokeAndWait(() -> {
            T result = null;
            try {
                result = callable.call();
                resultRef.set(result);
            } catch (Throwable e) {
                exceptionRef.set((E) e);
            }

        }, modalityState);
        if (exceptionRef.get() != null) {
            throw exceptionRef.get();
        }

        return resultRef.get();
    }
}
