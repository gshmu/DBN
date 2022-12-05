package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.dci.intellij.dbn.common.thread.ThreadProperty.BACKGROUND;

@Slf4j
public final class Background {
    private Background() {}

    public static void run(Project project, ThrowableRunnable<Throwable> runnable) {
        try {
            ThreadInfo threadInfo = ThreadMonitor.current();
            ExecutorService executorService = Threads.backgroundExecutor();
            executorService.submit(() -> {
                try {
                    ThreadMonitor.surround(
                            project,
                            threadInfo,
                            BACKGROUND,
                            runnable);
                } catch (ProcessCanceledException | UnsupportedOperationException | InterruptedException ignore) {
                } catch (Throwable e) {
                    log.error("Error executing background task", e);
                }
            });
        } catch (RejectedExecutionException e) {
            log.warn("Background execution rejected: " + e.getMessage());
        }
    }

    public static void run(Project project, AtomicReference<Thread> handle, ThrowableRunnable<Throwable> runnable) {
        try {
            Thread current = handle.get();
            if (current != null) {
                current.interrupt();
            }
            ThreadInfo threadInfo = ThreadMonitor.current();
            ExecutorService executorService = Threads.backgroundExecutor();
            executorService.submit(() -> {
                try {
                    try {
                        handle.set(Thread.currentThread());
                        ThreadMonitor.surround(
                                project,
                                threadInfo,
                                BACKGROUND,
                                runnable);
                    } finally {
                        handle.set(null);
                    }
                } catch (ProcessCanceledException | UnsupportedOperationException | InterruptedException ignore) {
                } catch (Throwable e) {
                    log.error("Error executing background task", e);
                }
            });
        } catch (RejectedExecutionException e) {
            log.warn("Background execution rejected: " + e.getMessage());
        }
    }

}
