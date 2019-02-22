package com.dci.intellij.dbn.common.load;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.dci.intellij.dbn.common.routine.BasicRunnable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class ProgressMonitor {
    public static ProgressIndicator getProgressIndicator() {
        return ProgressManager.getInstance().getProgressIndicator();
    }

    public static String getTaskDescription() {
        ProgressIndicator progressIndicator = getProgressIndicator();
        if (progressIndicator != null) {
            return progressIndicator.getText();
        }
        return null;
    }

    public static void setTaskDescription(String description) {
        ProgressIndicator progressIndicator = getProgressIndicator();
        if (progressIndicator != null) {
            progressIndicator.setText(description);
        }
    }

    public static void setSubtaskDescription(String subtaskDescription) {
        ProgressIndicator progressIndicator = getProgressIndicator();
        if (progressIndicator != null) {
            progressIndicator.setText2(subtaskDescription);
        }
    }

    public static boolean isCancelled() {
        ProgressIndicator progressIndicator = getProgressIndicator();
        return progressIndicator != null && progressIndicator.isCanceled();
    }

    public static void checkCancelled() {
        if (isCancelled()) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    public static <T, E extends Throwable> T invoke(ProgressIndicator progressIndicator, BasicCallable<T, E> callable) throws E {
        if (progressIndicator == null) {
            return callable.call();
        } else {
            AtomicReference<T> result = new AtomicReference<>();
            AtomicReference<E> exception = new AtomicReference<>();
            ProgressManager progressManager = ProgressManager.getInstance();
            progressManager.executeProcessUnderProgress(() -> {
                        try {
                            T callResult = callable.call();
                            result.set(callResult);
                        } catch (Throwable e) {
                            exception.set((E) e);
                        }
                    },
                    progressIndicator);

            if (exception.get() != null) {
                throw exception.get();
            }
            return result.get();
        }
    }

    public static <E extends Throwable> void invoke(ProgressIndicator progressIndicator, @NotNull BasicRunnable<E> runnable) throws E{
        if (progressIndicator == null) {
            runnable.run();
        } else {
            AtomicReference<E> exception = new AtomicReference<>();
            ProgressManager progressManager = ProgressManager.getInstance();
            progressManager.executeProcessUnderProgress(() -> {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    exception.set((E) e);
                }
            }, progressIndicator);

            if (exception.get() != null) {
                throw exception.get();
            }
        }
    }



}
