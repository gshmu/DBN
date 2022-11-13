package com.dci.intellij.dbn.common.routine;

import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GuardedRunnable implements Runnable {
    private final Runnable runnable;
    private GuardedRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public static GuardedRunnable of(Runnable runnable) {
        return new GuardedRunnable(runnable);
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (ProcessCanceledException | UnsupportedOperationException ignore) {
        } catch (Throwable e) {
            log.error("Failed to execute task", e);
        }

    }
}
