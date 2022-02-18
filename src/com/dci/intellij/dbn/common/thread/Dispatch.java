package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.Cancellable;
import com.dci.intellij.dbn.common.util.Commons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public final class Dispatch {
    private Dispatch() {}

    public static void run(Runnable runnable) {
        run(null, runnable);
    }

    public static void runConditional(Runnable runnable) {
        if (ThreadMonitor.isDispatchThread()) {
            try {
                runnable.run();
            } catch (ProcessCanceledException ignore) {}
        } else {
            run(null, runnable);
        }
    }

    public static void run(ModalityState modalityState, Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        modalityState = Commons.nvl(modalityState, application.getDefaultModalityState());
        application.invokeLater(() -> Cancellable.run(runnable), modalityState/*, ModalityState.NON_MODAL*/);
    }

    public static void invokeNonModal(Runnable runnable) {
        run(ModalityState.NON_MODAL, runnable);
    }

    public static <T, E extends Throwable> T callConditional(ThrowableCallable<T, E> callable) throws E{
        if (ThreadMonitor.isDispatchThread()) {
            return callable.call();
        } else {
            return call(callable);
        }
    }


    public static <T, E extends Throwable> T call(ThrowableCallable<T, E> callable) throws E{
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


    public static Alarm alarm(Disposable parentDisposable) {
        Failsafe.nd(parentDisposable);
        return new Alarm(parentDisposable);
    }

    public static void alarmRequest(@NotNull Alarm alarm, int delayMillis, boolean cancelRequests, @NotNull Runnable runnable) {
        runConditional(() -> {
            if (!alarm.isDisposed()) {
                if (cancelRequests) {
                    alarm.cancelAllRequests();
                }

                if (!alarm.isDisposed()) {
                    alarm.addRequest(runnable, delayMillis);
                }
            }
        });
    }
}
