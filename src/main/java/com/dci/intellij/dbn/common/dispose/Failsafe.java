package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;

@Slf4j
public class Failsafe {
    public static @NotNull <T> T nn(@Nullable T object) {
        if (object == null) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    @NotNull
    public static <T> T nd(@Nullable T object) {
        if (isNotValid(object)) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    public static <R, E extends Throwable> R guarded(R defaultValue, @Nullable ThrowableCallable<R, E> callable) throws E{
        try {
            return callable == null ? defaultValue : callable.call();
        } catch (ProcessCanceledException | IllegalStateException | AbstractMethodError ignore /*| UnsupportedOperationException*/){
            error(ignore);
            return defaultValue;
        } catch (Exception e) {
            error(e);
            // DBNE-4876 (????!!)
            if (e != AlreadyDisposedException.INSTANCE) {
                throw e;
            }
            return defaultValue;

        }

    }

    public static <E extends Throwable> void guarded(@Nullable ThrowableRunnable<E> runnable) throws E{
        try {
            if (runnable != null) runnable.run();
        } catch (ProcessCanceledException | IllegalStateException | AbstractMethodError ignore /*| UnsupportedOperationException*/){
            error(ignore);
        } catch (Exception e) {
            error(e);
            // DBNE-4876 (????!!)
            if (e != AlreadyDisposedException.INSTANCE) {
                throw e;
            }
        }

    }

    private static void error(Throwable exception) {
        if (Diagnostics.isFailsafeLoggingEnabled()) log.warn("Failsafe process failed", exception);
    }
}
